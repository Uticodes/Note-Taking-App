# Build a Simple Note-Taking App in Jetpack Compose with Room Persistence

This step-by-step guide walks you through building a simple N**ote-Taking app** using [**Jetpack Compose**](https://developer.android.com/compose) and [**Room**](https://developer.android.com/training/data-storage/room).

It is designed for developers who are comfortable with Kotlin but new to Jetpack Compose or Room.

By the end of this tutorial, you will have a working app where users can **add, view, edit, and delete notes**, with data stored locally on the device.

## **Quick Navigation**

1. [Introduction](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#introduction)
2. [Setup](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#setup)
3. [Create the Database](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#create-the-database)
4. [Build the UI](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#build-the-ui)
5. [Add Navigation](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#add-navigation)
6. [Run the App](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#run-the-app)
7. [Conclusion](https://github.com/Uticodes/Note-Taking-App/new/main?filename=README.md#conclusion)



## **Introduction**

Jetpack Compose is Google’s modern, declarative UI toolkit for Android development. It allows you to describe how your user interface should look and behave, automatically updating the screen when the underlying data changes. This eliminates the need for XML layouts and reduces boilerplate code.

Room is Android’s official persistence library built on top of SQLite. It simplifies data storage by providing compile-time query validation, migrations, and coroutine support.

In this tutorial, Jetpack Compose and Room are combined to create a lightweight **Note-Taking app**.

The app will cover these fundamental concepts:
- **State management** using ViewModel and StateFlow
- **Data persistence** with Room
- **Unidirectional data flow**, keeping the UI and data layers cleanly separated
- **Simple navigation** using Navigation Compose

This guide is beginner-friendly and focuses on practical steps, keeping the app small and straightforward. Once complete, you can extend it by adding features such as search, tagging, or cloud syncing.

## **Setup**

### Create a New Project

1. Open Android Studio and create a new project.
2. Choose **Empty Compose Activity**.
3. Name the project `NoteTakingApp` and click **Finish**.
4. Wait for Gradle to complete the initial sync.

### Add Dependencies

You will need **Room** for local storage and **Navigation Compose** for screen transitions.

Open `libs.versions.toml` and add the following dependencies:


```kotlin
[versions]
...
room = "2.7.2"
ksp = "2.0.21-1.0.26"
navigation = "2.9.0"

[libraries]
...
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

[plugins]
...
room = { id = "androidx.room", version.ref = "room" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

> **`androidx-room-ktx`** gives coroutines/Flow support; **`androidx-room-compiler`** generates DAOs/entities.
> 

Open **`project/build.gradle`** file and add the **`KSP`** and **`R**oom` plugin.

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    ...
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

> **Why** KSP? KSP runs the Room compiler, the Room plugin coordinates schema generation and migration checks during builds.
KSP is faster and more efficient than KAPT for generating Room database code.
> 


Open `app/build.gradle` and add the following dependencies:


```kotlin
plugins {
    ...
    // Enable the KSP plugin
    alias(libs.plugins.ksp) // KSP for Room codegen
    alias(libs.plugins.room) // Room Gradle plugin (enables schema tasks)
}

android {
    ...
    
    sourceSets {
        getByName("androidTest") {
            assets.srcDir(files("$projectDir/schemas"))
        }
    }
}

room {
		// JSON files will be written here at build time
    schemaDirectory("$projectDir/schemas")
}

dependencies {
	...

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    ...
}
```


Finally, click **Sync Now** when prompted.



## **Create the Database**

The data layer consists of three main components: **Entity**, **DAO**, and **Database**.

### 1. Entity

Create a `NoteEntity.kt` file inside a `data/local` package.

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long
)

```

> This defines a table named `notes` with four columns: `id`, `title`, `content`, and `timestamp`.
> 
> 
> The `id` is auto-generated for each note.
> 

---

### 2. DAO

Create a `NoteDao.kt` file in the same `data/local` package.

```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNoteEntities(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteEntityById(noteId: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteEntity(noteEntity: NoteEntity)

    @Update
    suspend fun updateNoteEntity(noteEntity: NoteEntity)

    @Delete
    suspend fun deleteNoteEntity(noteEntity: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNoteEntities()
}

```

> 
> 
> - `getAllNoteEntities()` returns a Flow that emits a list of notes whenever the database changes.
> - `getNoteEntityById()` gets a note by it's ID
> - `insertNoteEntity()` adds a new note or replaces an existing one with the same ID.
> - `updateNoteEntity()` modifies an existing note.
> - `deleteNoteEntity()` removes a note.
> - `deleteAllNoteEntities()` removes all notes.

---

### 3. Database

Create an `AppDatabase.kt` file in `data/local`.

```kotlin
@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "note_database")
                    .fallbackToDestructiveMigration() // For development. For production, implement proper migrations.
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

```

> 💡 Tip:
> 
> In a production app, you should implement proper migration scripts when updating the database schema.
> 

## **Create the model**
The domain layer consists of three main components: **Model**, **UseCase**, and **Repository**.
    
Create `NoteModel.kt` file in the `domain/model` 

`NoteModel` represents the core note data used throughout the app
    
    ```kotlin
    data class NoteModel(
        val id: Int = 0,
        val title: String,
        val content: String,
        val timestamp: Long
    )
    ```
    
Create `NotesUseCase.kt` file in the `domain/usecase`

`NotesUseCase` is a single entry point to all operations on notes (get/add/update/delete)
    
```kotlin
class NotesUseCase(private val repo: NoteRepository) {
    fun getNotes(): Flow<List<NoteModel>> = repo.getNotes()
    
    suspend fun getNoteById(id: Int): NoteModel? = repo.getNoteById(id)
    
    suspend fun addNote(note: NoteModel) = repo.addNote(note)
    
    suspend fun updateNote(note: NoteModel) = repo.updateNote(note)
    
    suspend fun deleteNote(note: NoteModel) = repo.deleteNote(note)
    
    suspend fun deleteAll() = repo.deleteAllNotes()
}
```
    

## **Data → Domain glue**

Create `NoteRepository.kt` file in `data/repository`

`NoteRepository` (interface) and `NoteRepositoryImpl` (maps NoteEntities ⇄ NoteModels)
    
```kotlin
interface NoteRepository {
    fun getNotes(): Flow<List<NoteModel>>
    suspend fun getNoteById(id: Int): NoteModel?
    suspend fun addNote(note: NoteModel)
    suspend fun updateNote(note: NoteModel)
    suspend fun deleteNote(note: NoteModel)
    suspend fun deleteAllNotes()
}
```
    
```kotlin
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val mapper: Mapper
) : NoteRepository {
    
    override fun getNotes(): Flow<List<NoteModel>> {
        return noteDao.getAllNoteEntities().map { entities ->
            entities.map(mapper::mapNoteEntityToNote)
        }
    }
    
    override suspend fun getNoteById(id: Int): NoteModel? {
        return noteDao.getNoteEntityById(id)?.let(mapper::mapNoteEntityToNote)
    }
    
    override suspend fun addNote(note: NoteModel) {
        noteDao.insertNoteEntity(mapper.mapNoteToNoteEntity(note))
    }
    
    override suspend fun updateNote(note: NoteModel) {
        noteDao.updateNoteEntity(mapper.mapNoteToNoteEntity(note))
    }
    
    override suspend fun deleteNote(note: NoteModel) {
        noteDao.deleteNoteEntity(mapper.mapNoteToNoteEntity(note))
    }
    
    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNoteEntities()
    }
}
```
    
Create `Mapper.kt` file in `util`
`Mapper` is used to map entity and model data from database to repository.
    
```kotlin
class Mapper {
    fun mapNoteEntityToNote(entity: NoteEntity): NoteModel {
        return NoteModel(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            timestamp = entity.timestamp
        )
    }

    fun mapNoteToNoteEntity(note: NoteModel): NoteEntity {
        return NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            timestamp = note.timestamp
        )
    }
}
```

### ViewModel Setup

Create a `NotesViewModel.kt` and `NoteDetailViewModel.kt` file in the `presentation` package.

`NotesViewModel` handles getting and deleting notes and reporting errors, if any, it interacts with `NotesUseCase`.

```kotlin
class NotesViewModel(
    private val notesUseCase: NotesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes: StateFlow<List<NoteModel>> = _notes.asStateFlow()

    private val _effects = Channel<NotesEffect>(Channel.BUFFERED)
    val effects: Flow<NotesEffect> = _effects.receiveAsFlow()

    init {
        getNotes()
    }

    private fun getNotes() {
        notesUseCase.getNotes().onEach { notes ->
            _notes.value = notes
        }.launchIn(viewModelScope)

        savedStateHandle.getStateFlow("result", "")
            .filter { it.isNotBlank() }
            .onEach {
                _effects.send(NotesEffect.ShowMessage(it))
                savedStateHandle["result"] = ""
            }
            .launchIn(viewModelScope)
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            notesUseCase.deleteAll()
            _effects.send(NotesEffect.ShowMessage("All notes deleted"))
        }
    }
}

sealed interface NotesEffect { data class ShowMessage(val text: String): NotesEffect }

```
---
<br>

`NoteDetailViewModel` loads an existing note (if noteId > 0), exposes title/content flows, and saves or deletes via the use case. It also saves the inputs (title and content) as a draft using SavedStateHandle, making unsaved text survive screen rotation or process recreation. It is the safest “traditional” approach.

```kotlin
class NoteDetailViewModel(
    private val noteId: Int,
    private val notesUseCase: NotesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _note = MutableStateFlow<NoteModel?>(null)
    val note: StateFlow<NoteModel?> = _note.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    init {
        if (noteId != 0) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: Int) {
        viewModelScope.launch {
            val existing = notesUseCase.getNoteById(noteId)
            _note.value = existing
            if (existing != null) {
                if (_title.value.isEmpty())  _title.value = existing.title
                if (_content.value.isEmpty()) _content.value = existing.content
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        savedStateHandle[KEY_TITLE] = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
        savedStateHandle[KEY_CONTENT] = newContent
    }

    fun saveNote(onNavigateBack: () -> Unit) {
        viewModelScope.launch {
            val title = _title.value.trim()
            val ccontent = _content.value.trim()
            if (ttitle.isEmpty()) return@launch

            val model = NoteModel(
                id = if (noteId > 0) noteId else 0,
                title = title,
                content = ccontent,
                timestamp = System.currentTimeMillis()
            )

            if (noteId > 0) notesUseCase.updateNote(model) else notesUseCase.addNote(model)
            clearDraft()
            onNavigateBack()
        }
    }

    private fun clearDraft() {
        savedStateHandle[KEY_TITLE] = ""
        savedStateHandle[KEY_CONTENT] = ""
    }

    fun deleteNote(onComplete: () -> Unit) {
        viewModelScope.launch {
            _note.value?.let { noteToDelete ->
                notesUseCase.deleteNote(noteToDelete)
                onComplete()
            }
        }
    }

    companion object {
        private const val KEY_TITLE = "draft_title"
        private const val KEY_CONTENT = "draft_content"

        fun Factory(noteId: Int, notesUseCase: NotesUseCase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val handle = createSavedStateHandle()
                    NoteDetailViewModel(
                        noteId = noteId,
                        notesUseCase = notesUseCase,
                        savedStateHandle = handle
                    )
                }
            }
    }
}
```

 
> 
> The ViewModel manages the app’s state and interacts with the DAO.
> 
> The `notes` property automatically updates the UI whenever the database changes.
> 

---

## **Build the UI**

The app has two main screens:

Notes List Screen – Displays all saved notes.

Note Detail Screen – Allows adding or editing a note.


### Notes List Screen

Create a `NotesScreen.kt` file in the `presentation` package.

`NotesScreen`  has a `Scaffold` with a **`TopAppBar`** (to Delete All notes), a **`FloatingActionButton`** (“+” Add note), and a `LazyColumn` to show the list of notes.

The list (notes) observes a `StateFlow<List<NoteModel>>` provided by the `NotesViewModel` to display the current list of notes.

```kotlin
@Composable
fun NotesScreen(viewModel: NotesViewModel, onAddNote: () -> Unit, onNoteClick: (NoteEntity) -> Unit) {
    val notes by viewModel.notes.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Notes") },
                actions = {
                    if(notes.isNotEmpty()) {
                        IconButton(onClick = viewModel::deleteAllNotes) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete All Notes")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notes yet. Tap + to add one.")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onNoteClick(note) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

```

> 💡 Tip:
> 
> 
> Using `Flow` with Room allows the `LazyColumn` to automatically refresh whenever data changes.
> 

---

### Note Detail Screen

Create a `NoteDetailScreen.kt` file in the same `presentation` package.

NoteDetailScreen has two text input fields (title & content) and actions: Back, Delete (existing note only), and Save.

```kotlin
@Composable
fun NoteDetailScreen(viewModel: NotesViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.addNote(title, content)
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}

```

---

## **Add Navigation**

To navigate between the list screen and the detail screen, use **Navigation Compose**.

In your `NavHost`, create a ViewModel for each destination and pass them to the corresponding screen:

```kotlin
composable(Screen.NoteList.route) {
    val vm: NotesViewModel = viewModel(factory = NotesViewModelFactory)
    NotesScreen(
        viewModel = vm,
        onNoteClick = { id -> navController.navigate(Screen.NoteDetail.create(id)) },
        onAddNoteClick = { navController.navigate(Screen.NoteDetail.create(0)) }
    )
}

composable(
    route = Screen.NoteDetail.route,
    arguments = listOf(navArgument("noteId") { type = NavType.IntType })
) { backStackEntry ->
    val id = backStackEntry.arguments?.getInt("noteId") ?: 0
    val app = LocalContext.current.applicationContext as NoteApp
    val dvm: NoteDetailViewModel = viewModel(
        factory = NoteDetailVmFactory.create(id, app.appModule.notesUseCase)
    )
    NoteDetailScreen(viewModel = dvm, onNavigateBack = { navController.popBackStack() })
}

```

In `MainActivity.kt`:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTakingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val application = application as NoteApp

                    // initialize the AppNavHost
                    AppNavHost(
                        navController = navController,
                        application = application
                    )
                }
            }
        }
    }
}

```

---

## **Run the App**

1. Connect an Android device or start an emulator.
2. In Android Studio, click **Run ▶**.
3. Add a note by tapping the **+ button**.
4. View it in the list.
5. Tap the back arrow to return to the list.

If you edit or delete a note, the list will automatically update in real-time.

---

## **Conclusion**

In this guide, you have:

- Set up **Room** to persist notes locally.
- Built a **responsive UI** with Jetpack Compose.
- Managed state using **ViewModel** and **Flow**.
- Added **navigation** between screens.

This app forms a solid foundation for more advanced features, such as:

- Search and filtering
- Note pinning or colour coding
- Syncing notes to the cloud with Firebase or other services
- Writing unit tests for the ViewModel and DAO

By mastering these fundamentals, you are now prepared to build scalable, modern Android applications with clean architecture and a seamless user experience.

---

Full Code on [Github](https://github.com/Uticodes/Note-Taking-App)

**Happy coding!**
