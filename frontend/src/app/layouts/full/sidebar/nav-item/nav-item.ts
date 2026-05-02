export interface NavGroupItem {
    id?: number;
    name?: string;
    applications?: NavItem[];
  }

export interface NavItem {
    id?: number;
    name?: string;
    description?:string;
    disabled?: boolean;
    external?: boolean;
    twoLines?: boolean;
    chip?: boolean;
    icon?: string;
    isActive?: boolean;
    navCap?: string;
    chipContent?: string;
    chipClass?: string;
    subtext?: string;
    route?: string;
    menus?: NavItem[];
    ddType?: string;
}