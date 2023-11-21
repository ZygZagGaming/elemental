package core


var profiling = false
fun log(message: String, force: Boolean = false) {
    if (profiling || force) console.log(message)
}

fun log(message: Any?, force: Boolean = false) {
    if (profiling || force) console.log(message?.toString())
}