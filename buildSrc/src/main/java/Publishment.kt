import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object Publishment {

    private const val SNAPSHOT_SUFFIX = "SNAPSHOT"

    // publish properties
    private const val PROPS_IS_LOCAL_REPO = "localRepo"
    private const val PROPS_GROUP_ID = "groupId"
    private const val PROPS_ARTIFACT_ID = "artifactId"
    private const val PROPS_ART_VERSION = "version"
    private const val PROPS_ENABLE_SNAPSHOT = "snapshot"
    private const val PROPS_REPO_URL = "repoUrl"
    private const val PROPS_REPO_URL_SNAPSHOT = "repoUrlSnapshot"
    private const val PROPS_MAVEN_USERNAME = "username"
    private const val PROPS_MAVEN_TOKEN = "token"

    // publish pom info
    private const val PROPS_POM_NAME = "name"
    private const val PROPS_POM_DESC = "description"
    private const val PROPS_POM_URL = "url"
    private const val PROPS_POM_EMAIL = "email"

    // publish gradle cmd params
    private const val GRADLE_P_PUB_ENABLE_SNAPSHOT = "PUB_ENABLE_SNAPSHOT"
    private const val GRADLE_P_PUB_ART_VERSION = "PUB_ART_VERSION"
    private const val GRADLE_P_PUB_REPO_URL = "PUB_REPO_URL"
    private const val GRADLE_P_PUB_IS_LOCAL_REPO = "PUB_IS_LOCAL_REPO"
    private const val GRADLE_P_PUB_MAVEN_USERNAME = "PUB_MAVEN_USERNAME"
    private const val GRADLE_P_PUB_MAVEN_TOKEN = "PUB_MAVEN_TOKEN"

    /**
     * load publish properties
     *
     * @param project proj
     * @param files   properties files, 支持多文件列表, 若存在相同字段，顺序靠后的字段会覆盖靠前的字段
     * @return publish info
     */
    fun loadProperties(project: Project, vararg files: File): PublishInfo {
        val props = Properties()
        files.forEach { file ->
            println("> load properties file: $file")
            props.load(FileInputStream(file))
        }

        // artifact info
        val groupId = props.get(PROPS_GROUP_ID).toString()
        val artifactId = props.get(PROPS_ARTIFACT_ID).toString()
        val version = props.getVersion(project)

        // repo info
        val isLocalRepo = props.isLocalRepo(project)
        val repoUrl = props.getRepoUrl(project)
        val username = props.getUsername(project)
        val token = props.getToken(project)
        val snapshot = props.isSnapshot(project)

        // pom info
        val pomName = props.get(PROPS_POM_NAME)
        val pomDesc = props.get(PROPS_POM_DESC)
        val pomUrl = props.get(PROPS_POM_URL)
        val pomEmail = props.get(PROPS_POM_EMAIL)
        val pomInfo = PomInfo().apply {
            this.name = pomName.toString()
            this.description = pomDesc.toString()
            this.url = pomUrl.toString()
            this.email = pomEmail.toString()
        }

        val info = PublishInfo(
            props, project.rootDir, groupId, artifactId, version, repoUrl
        ).apply {
            this.snapshot = snapshot
            this.username = username
            this.token = token
            this.isLocalRepo = isLocalRepo
            this.pomInfo = pomInfo
        }
        println("> Publish info: $info")
        return info
    }

    /**
     * get maven username
     *
     * @param project proj
     * @return maven username
     */
    fun Properties.getUsername(project: Project): String {
        val projectProps = project.getProperties()
        val username = projectProps.get(GRADLE_P_PUB_MAVEN_USERNAME) ?: this[PROPS_MAVEN_USERNAME]
        return username.toString()
    }

    /**
     * get maven token
     *
     * @param project proj
     * @return maven token
     */
    fun Properties.getToken(project: Project): String {
        val projectProps = project.getProperties()
        val token = projectProps.get(GRADLE_P_PUB_MAVEN_TOKEN) ?: this[PROPS_MAVEN_TOKEN]
        return token.toString()
    }

    /**
     * get version info
     * - read cmd params first, set use `-PPUB_ART_VERSION`
     *
     * @param project proj
     * @return version name
     */
    fun Properties.getVersion(project: Project): String {
        val projectProps = project.getProperties()
        val version = projectProps.get(GRADLE_P_PUB_ART_VERSION) ?: this[PROPS_ART_VERSION]
        return if (this.isSnapshot(project)) {
            appendSnapshot(version.toString())
        } else {
            version.toString()
        }
    }

    /**
     * get repo url
     * - read cmd params first, set use `-PPUB_LOCAL_REPO` and `-PPUB_REPO_URL`
     *
     * @param project proj
     * @return repo url
     */
    fun Properties.getRepoUrl(project: Project): String {
        // check is local repo
        val local = this.isLocalRepo(project)
        if (local) {
            return "file://${project.rootDir.path}/repo"
        }

        // read cmd params first
        val projectProps = project.getProperties()
        // if cmd params Not Set, use default this properties
        val repoUrl = projectProps[GRADLE_P_PUB_REPO_URL]
        return if (repoUrl != null) {
            repoUrl.toString()
        } else {
            if (this.isSnapshot(project)) {
                this[PROPS_REPO_URL_SNAPSHOT].toString()
            } else {
                this[PROPS_REPO_URL].toString()
            }
        }
    }

    /**
     * Is SNAPSHOT product
     * - read cmd params first, set use `-PPUB_ENABLE_SNAPSHOT`
     *
     * @param project proj
     * @return true or false
     */
    fun Properties.isSnapshot(project: Project): Boolean {
        // read cmd params first
        val projectProps = project.getProperties()
        // if cmd params Not Set, use default this properties
        val snapshot = projectProps[GRADLE_P_PUB_ENABLE_SNAPSHOT] ?: this[PROPS_ENABLE_SNAPSHOT]
        return snapshot.toString().toBoolean()
    }

    /**
     * Is local repo
     * - read cmd params first, set use `-PPUB_LOCAL_REPO`
     *
     * @param project proj
     * @return true or false
     */
    fun Properties.isLocalRepo(project: Project): Boolean {
        // read cmd params first
        val projectProps = project.getProperties()
        // if cmd params Not Set, use default this properties
        val localRepo = projectProps[GRADLE_P_PUB_IS_LOCAL_REPO] ?: this[PROPS_IS_LOCAL_REPO]
        return localRepo.toString().toBoolean()
    }

    /**
     * append SNAPSHOT
     *
     * @param version version name
     * @return version-SNAPSHOT
     */
    @JvmStatic
    fun appendSnapshot(version: String): String {
        if (version.endsWith(SNAPSHOT_SUFFIX)) {
            return version
        }
        return "$version-$SNAPSHOT_SUFFIX"
    }
}