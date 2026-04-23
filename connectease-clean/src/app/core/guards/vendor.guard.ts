import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const vendorGuard = () => {
  const router = inject(Router);
  if (localStorage.getItem('role') === 'vendor') return true;
  return router.createUrlTree(['/']);
};
