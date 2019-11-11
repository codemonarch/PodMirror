import com.rarnu.common.File
import com.rarnu.common.runCommand

expect val osName: String

fun greeting() = "iOS Mirror ($osName)\n"

fun gitCommand(path: String, cmd: String) = runCommand("git -C $path $cmd").output.trim()
fun command(cmd: String) = runCommand(cmd).output.trim()

fun main(args: Array<String>) {
    println(greeting())
    println("Environment validation...")
    val repoPath = command("echo \$HOME") + "/.cocoapods/repos/master/Specs"
    val localPath = command("echo \$LOCAL_SPEC")

    if (!File(repoPath).exists()) {
        println("$repoPath not exists, please install cocoapods and sync at least one time.")
        return
    }
    if (!File(localPath).exists()) {
        println("Local mirror path not exists, please config it in environment variables named LOCAL_SPEC.")
        return
    }
    println("updating pod repo...")
    command("pod repo update")
    println("updating local spec...")
    gitCommand(localPath, "pull")

    repoList.forEach { r ->
        println("sync $r ...")
        val find = command("find $repoPath -name $r")
        val loc = "$localPath/$r"
        val listDiff = File(find).list() - File(loc).list()
        listDiff.forEach { d ->
            command("cp -r $find/$d $loc/")
        }
    }

    println("sync complete, commit change...")
    gitCommand(localPath, "add -- .")
    gitCommand(localPath, "commit -m \"sync pod repo\"")
    gitCommand(localPath, "push")

    println("done.")
}
