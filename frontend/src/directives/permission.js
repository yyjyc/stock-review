import { useUserStore } from '@/store/user'

export const permission = {
  mounted(el, binding) {
    const { isAdmin } = useUserStore()
    const requiredRoles = binding.value

    if (Array.isArray(requiredRoles) && requiredRoles.length > 0) {
      const hasPermission = requiredRoles.includes('ADMIN') && isAdmin()
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

export function setupPermissionDirective(app) {
  app.directive('permission', permission)
}
