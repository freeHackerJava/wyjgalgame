import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material", module = "material")
        exclude(group = "org.jetbrains.compose.material", module = "material-icons-core")
        exclude(group = "org.jetbrains.compose.material", module = "material-icons-extended")
    }

    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    val voyagerVersion = "1.1.0-beta03"
    implementation("cafe.adriel.voyager:voyager-navigator:${voyagerVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation(libs.kotlinx.serialization.json)
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WYJGalGame"
            packageVersion = "1.0.0"
            description = "合肥 · 周日编程班：从 CSP-S 1= 出发，陪四位初一学生走到 NOI Au 的养成向 galgame。"
            vendor = "org.example.project"
        }
    }
}
