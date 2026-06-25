package com.easy_permissions.permission_enums

/**
 * Represents the detailed outcome of a permission request.
 * Helps the developer know exactly what actions the library took.
 */

enum class PermissionStatus (val status : String) {
    ACCESS_GRANTED("✅ Access Granted"),
    ACCESS_DENIED_BY_USER("❌ Access Denied by User"),
    ACCESS_DENIED_OPENING_PHONE_SETTINGS("❌ Access Denied by User, Opening phone settings..")

}