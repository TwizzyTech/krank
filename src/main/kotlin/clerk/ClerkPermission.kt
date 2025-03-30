package twizzy.tech.clerk

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ClerkPermission(val value: String)