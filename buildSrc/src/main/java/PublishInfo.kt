import java.io.File
import java.net.URI
import java.util.Properties

/**
 * publish info
 */
class PublishInfo(

    /**
     * original props
     */
    val props: Properties,

    /**
     * root dir
     */
    val rootDir: File,

    /**
     * publish groupId
     */
    val groupId: String,

    /**
     * publish artifactId
     */
    val artifactId: String,

    /**
     * publish version
     */
    val version: String,

    /**
     * publish repo url
     */
    val repoUrl: String
) {

    /**
     * is snapshot
     */
    var snapshot: Boolean = false

    /**
     * repo username
     */
    var username: String = ""

    /**
     * repo token
     */
    var token: String = ""

    /**
     * check is local repo
     */
    var isLocalRepo: Boolean = false

    /**
     * pom extra info
     */
    var pomInfo: PomInfo? = null

    override fun toString(): String {
        return StringBuilder("{")
            .append("\"product\": \"").append(getFullName())
            .append("\", \"repo\": \"").append(repoUrl)
            .append("\"}").toString()
    }

    operator fun get(key: String): String? {
        return props.getProperty(key)
    }

    /**
     * dump info
     *
     * @return dump info
     */
    fun dump(): String {
        return StringBuilder("{")
            .append("username:").append(username)
            .append(", token:").append(token)
            .append(", isLocalRepo:").append(isLocalRepo)
            .append("}").toString()
    }

    /**
     * create repo URI
     *
     * @return repo URI
     */
    fun createRepoURI(name: String = ""): URI {
        return if (name.isEmpty()) {
            URI.create(repoUrl)
        } else {
            if (isLocalRepo) {
                URI.create("file://${rootDir.path}/repo")
            } else {
                val url = if (snapshot) {
                    get("${name}_repoUrlSnapshot")
                } else {
                    get("${name}_repoUrl")
                } ?: ""
                URI.create(url)
            }
        }
    }

    /**
     * get profuct full name
     *
     * @return full name
     */
    fun getFullName(): String {
        return "$groupId:$artifactId:$version"
    }
}