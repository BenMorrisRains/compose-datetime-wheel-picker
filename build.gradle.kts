plugins {
  alias(libs.plugins.multiplatform).apply(false)
  alias(libs.plugins.compose).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.android.application).apply(false)
  alias(libs.plugins.android.library).apply(false)
  alias(libs.plugins.maven.publish).apply(false)
  alias(libs.plugins.ksp).apply(false)
}

// For JitPack - apply publishing to the datetime-wheel-picker module
subprojects {
  if (name == "datetime-wheel-picker") {
    apply(plugin = "maven-publish")
  }
}
