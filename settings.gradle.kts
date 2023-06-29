rootProject.name = "gradle-oci-junit-jupiter"

if (file("../oci-registry").exists()) {
    includeBuild("../oci-registry")
}
