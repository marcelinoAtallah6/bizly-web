package com.um.api.service.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.um.api.model.menu.UmMenu;

/**
 * Builds arbitrary-depth menu trees from a flat {@code UM_MENUS} list ordered by
 * {@code sort_order, name}. JPA {@code @OneToMany} children collections only
 * reflect direct children; this helper wires grandchildren and deeper levels.
 */
public final class MenuTreeSupport {

	private MenuTreeSupport() {
	}

	public static Long parentIdOf(UmMenu menu) {
		return menu.getParentMenu() != null ? menu.getParentMenu().getId() : null;
	}

	/**
	 * @param flat rows for one application, typically sorted by sort_order then name
	 * @param include whether a menu row should appear in the output tree
	 * @param mapper converts an included {@link UmMenu} to the node type
	 * @param childrenAccessor returns the mutable child list on a node
	 */
	public static <T> List<T> buildTree(List<UmMenu> flat, Predicate<UmMenu> include,
			Function<UmMenu, T> mapper, Function<T, List<T>> childrenAccessor) {
		Map<Long, T> byId = new HashMap<>();
		for (UmMenu m : flat) {
			if (include.test(m)) {
				byId.put(m.getId(), mapper.apply(m));
			}
		}
		List<T> roots = new ArrayList<>();
		for (UmMenu m : flat) {
			T node = byId.get(m.getId());
			if (node == null) {
				continue;
			}
			Long parentId = parentIdOf(m);
			if (parentId == null || !byId.containsKey(parentId)) {
				roots.add(node);
			} else {
				List<T> siblings = childrenAccessor.apply(byId.get(parentId));
				if (siblings != null) {
					siblings.add(node);
				}
			}
		}
		return roots;
	}

	/** Sorts each tree level by {@code comparator} so sibling order follows {@code sort_order}, not flat-list position. */
	public static <T> void sortTree(List<T> roots, Comparator<T> comparator, Function<T, List<T>> childrenAccessor) {
		if (roots == null || roots.isEmpty()) {
			return;
		}
		roots.sort(comparator);
		for (T node : roots) {
			List<T> children = childrenAccessor.apply(node);
			if (children != null && !children.isEmpty()) {
				sortTree(children, comparator, childrenAccessor);
			}
		}
	}

	public static Comparator<UmMenu> menuSortComparator() {
		return Comparator
				.comparing(UmMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(UmMenu::getName, Comparator.nullsLast(String::compareToIgnoreCase));
	}
}
