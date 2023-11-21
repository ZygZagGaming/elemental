package core

object Options {
    var saveInterval = 5.0
    var saveMode = SaveMode.LOCAL_STORAGE
    var autosaving = true
    var autoclickerDockSnapDistance = 50.0
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun toggleAutosaving() {
    Options.autosaving = !Options.autosaving
}