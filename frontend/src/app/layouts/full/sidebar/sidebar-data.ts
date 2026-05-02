// import { Injectable } from '@angular/core';
// import { NavGroupItem } from './nav-item/nav-item';
// import axios from 'axios';
// import { GlobalConstants } from 'src/app/common/GlobalConstants';

// // export const navItems: NavItem[] = [
// //   {
// //     navCap: 'Home',
// //   },
// //   {
// //     displayName: 'Dashboard',
// //     iconName: 'layout-dashboard',
// //     route: '/dashboard',
// //     children: [
// //       { displayName: "Summary", route: "/dashboard/summary", iconName: "chart-bar" },
// //       { displayName: "Appointments", route: "/dashboard/appointments", iconName: "calendar" },
// //       { displayName: "Customer Visitors", route: "/dashboard/visitors", iconName: "user-check" }
// //     ]
// //   },
// //   {
// //     displayName: 'KYC Management',
// //     iconName: 'users-group',
// //     route: '/kyc',
// //   },
// //   {
// //     displayName: 'Store Management',
// //     iconName: 'clipboard-list',
// //     route: '/store',
// //   },
// //   {
// //     displayName: 'Appointment',
// //     iconName: 'calendar-cog',
// //     route: '/apt',
// //   },
// //   {
// //     displayName: 'Payments',
// //     iconName: 'brand-cashapp',
// //     route: '/pay',
// //   },
// //   {
// //     displayName: 'Global Cofiguration',
// //     iconName: 'settings-plus',
// //     route: '/conf',
// //   },
// //   {
// //     navCap: 'Setup',
// //   },
// //   {
// //     displayName: 'User Management',
// //     iconName: 'user-plus',
// //     route: '/um',
// //   },
  
// //   {
// //     displayName: 'Query Builder',
// //     iconName: 'file-type-sql',
// //     route: '/qbe',
// //   },
// //   {
// //     displayName: 'Report Builder',
// //     iconName: 'file-analytics',
// //     route: '/rpt',
// //   },
// //   {
// //     displayName: 'Dashboard Builder',
// //     iconName: 'dashboard',
// //     route: '/dash',
// //   },
// //   {
// //     displayName: 'API Builder',
// //     iconName: 'api',
// //     route: '/api',
// //   },
// //   {
// //     navCap: 'Ui Components',
// //   },
// //   {
// //     displayName: 'Badge',
// //     iconName: 'rosette',
// //     route: '/ui-components/badge',
// //   },
// //   {
// //     displayName: 'Chips',
// //     iconName: 'poker-chip',
// //     route: '/ui-components/chips',
// //   },
// //   {
// //     displayName: 'Lists',
// //     iconName: 'list',
// //     route: '/ui-components/lists',
// //   },
// //   {
// //     displayName: 'Ag Grid',
// //     iconName: 'table',
// //     route: '/ui-components/ag-grid',
// //   },
// //   {
// //     displayName: 'Menu',
// //     iconName: 'layout-navbar-expand',
// //     route: '/ui-components/menu',
// //   }, 
// //   {
// //     displayName: 'Tooltips',
// //     iconName: 'tooltip',
// //     route: '/ui-components/tooltips',
// //   },
// //   {
// //     navCap: 'Auth',
// //   },
// //   {
// //     displayName: 'Login',
// //     iconName: 'lock',
// //     route: '/authentication/login',
// //   },
// //   {
// //     displayName: 'Register',
// //     iconName: 'user-plus',
// //     route: '/authentication/register',
// //   },
// //   {
// //     navCap: 'Extra',
// //   },
// //   {
// //     displayName: 'Icons',
// //     iconName: 'mood-smile',
// //     route: '/extra/icons',
// //   },
// //   {
// //     displayName: 'Sample Page',
// //     iconName: 'aperture',
// //     route: '/extra/sample-page',
// //   },
// // ];



// export const navItems: NavGroupItem[] =  [
//   {
//     id: 1,
//     name: 'Home',
//     applications: [
//       {
//         id: 39,
//         name: 'Dashboard',
//         description: 'Description here',
//         icon: 'layout-dashboard',
//         route: '/dashboard',
//         isActive: true,
//         menus: []
//       },
//       {
//         id: 40,
//         name: 'KYC Management',
//         description: 'Description here',
//         icon: 'users-group',
//         route: '/kyc',
//         isActive: true,
//         menus: [
//           {
//             id: 122,
//             name: 'Customers',
//             route: '/kyc/customers',
//             icon: 'user-circle',
//             isActive: true,
//             menus: []
//           }
//         ]
//       }
//     ]
//   },
//   {
//     id: 2,
//     name: 'Setup',
//     applications: [
//       {
//         id: 39,
//         name: 'Dashboard',
//         description: 'Description here',
//         icon: 'layout-dashboard',
//         route: '/dashboard',
//         isActive: true,
//         menus: []
//       },
//       {
//         id: 40,
//         name: 'KYC Management',
//         description: 'Description here',
//         icon: 'users-group',
//         route: '/kyc',
//         isActive: true,
//         menus: [
//           {
//             id: 122,
//             name: 'Customers',
//             route: '/kyc/customers',
//             icon: 'user-circle',
//             isActive: true,
//             menus: []
//           }
//         ]
//       }
//     ]
//   }
// ];


// // // @Injectable({
// // //   providedIn: 'root'
// // // })
// // // export class NavService {
// // //   private apiUrl = GlobalConstants.API_ENDPOINTS.application.getAll; // Update the URL if necessary

// // //   async getNavItems(): Promise<NavGroupItem[]> {
// // //     console.log("in api")
// // //     try {
// // //       const response = await axios.get<NavGroupItem[]>(this.apiUrl);
// // //       console.log(response)
// // //       return response.data;
// // //     } catch (error) {
// // //       console.error('Error fetching nav items:', error);
// // //       throw error;
// // //     }
// // //   }
// // }

