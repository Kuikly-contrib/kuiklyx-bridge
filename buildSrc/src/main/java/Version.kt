/**
 * Version
 */
object Version {

    const val DEFAULT_KOTLIN_VERSION = "1.7.20"

    /**
     * append sub version, support -SNAPSHOT
     *
     * @param original   original version
     * @param subversion sub version
     * @return version-subversion
     */
    fun appendSubVersion(original: String, subversion: String): String {
        return if (!original.endsWith("-SNAPSHOT")) {
            "${original}-${subversion}"
        } else {
            val version = original.removeSuffix("-SNAPSHOT")
            "${version}-${subversion}-SNAPSHOT"
        }
    }

    /**
     * append Kuikly-Render-Android Version
     * - 按照 kuikly render android 版本, 1.7.20 无需拼接 kotlin 版本
     * - 其他需拼接 kotlin 版本
     *
     * @param coreVersion   kuikly core version
     * @param kotlinVersion kotlin version
     * @return kuikly render android version
     */
    fun appendKuiklyRenderVersion(coreVersion: String, kotlinVersion: String): String {
        return if (kotlinVersion == DEFAULT_KOTLIN_VERSION) {
            coreVersion
        } else {
            appendSubVersion(coreVersion, kotlinVersion)
        }
    }
}