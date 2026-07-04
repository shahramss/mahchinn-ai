
@Composable
fun SearchableProjectDropdown(
    projects: List<Project>,
    selectedId: Long?,
    onSelect: (Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val filtered = remember(search, projects) {
        if (search.isBlank()) projects else projects.filter { it.name.contains(search, true) }
    }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(projects.find { it.id == selectedId }?.name ?: "انتخاب پروژه")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو") }
            )

            filtered.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = {
                        onSelect(p)
                        expanded = false
                        search = ""
                    }
                )
            }
        }
    }
}

@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    selectedProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(initialTitle, initialDescription, selectedProjectId); onDismiss() }) {
                Text("OK")
            }
        },
        text = { Text("Task Editor") }
    )
}

@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    priority: Int?,
    projectId: Long?,
    selectedProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    TaskEditorDialog(titleText, initialTitle, initialDescription, selectedProjectId, onDismiss, onSave)
}

@Composable
fun JalaliDateDialog(
    initialDate: String? = null,
    onSave: (String) -> Unit = {},
    onClear: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {}

@Composable
fun TaskAlarmDialog(
    initialDate: String? = null,
    hour: Int? = null,
    minute: Int? = null,
    onSave: (String, Int, Int) -> Unit = {_,_,_ ->},
    onDismiss: () -> Unit = {}
) {}

@Composable
fun VoiceOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    prompt: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { if (label != null) Text(label) },
        singleLine = singleLine
    )
}
