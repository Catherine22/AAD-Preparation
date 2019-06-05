# AAD-Preparation

Associate Android Developer Study Guide   
https://developers.google.com/training/certification/associate-android-developer/study-guide/   

# Navigation
- [Material design]         
- [Localisation]      
- [Device compatibility]        
- [Lifecycle]       
- [App components]      
- [Working in the background]      
- [Notification]      
- [Accessibility Features]        
- [Day/Night Mode]      
- [Styles]       
- [Jetpack]      
- [Kotlin]

# Material design
Add the material dependency in build.gradle
```gradle
implementation 'com.google.android.material:material:1.0.0'
```

- ```Toast``` vs ```Snackbar```       

|             | Toast                                                                | Snackbar            |
|-------------|----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Overview    | A toast provides simple feedback about an operation in a small popup | Snackbars provide lightweight feedback about an operation                                                                                         |
| Interaction | Toasts automatically disappear after a timeout                       | Snackbar could either automatically disappear after a timeout, or be manually closed                                                           |
| More info   |                                                                      | 1. Having a CoordinatorLayout in your view hierarchy allows Snackbar to enable certain features 2. Snackbars can contain an action such as "undo"|
| Sample code | [BaseActivity]                                                       | [BaseActivity]      |


[Read more - Toasts](https://developer.android.com/guide/topics/ui/notifiers/toasts)       
[Read more - Snackbar](https://developer.android.com/reference/android/support/design/widget/Snackbar)       


## Layouts
- AppBarLayout -> [Any activity in this app      
- BottomSheet -> [ContentProviderFragment]      
- Chip -> [NotificationActivity]      
- ConstraintLayout -> [rv_album_item]      
- FloatingActionButton -> [MainActivity]      
- MaterialButton -> [BackgroundServiceFragment]      
- MaterialCardView -> [BackgroundActivity]      
- NavigationView -> [MainActivity]      
- RadioGroup -> [BackgroundActivity]      
- Seekbar (sliders) -> [BackgroundActivity]      
- TabLayout -> [AppComponentsActivity]      
- TextInputLayout, TextInputEditText -> [NotificationActivity]      
- RecyclerView (grid) -> [ArtistsFragment]   
- RecyclerView (linear) -> [AlbumsFragment]   
- RecyclerView (drag + swipe) -> [PlaylistFragment], [PlaylistAdapter], [RecyclerViewItemTouchHelper]    
- RecyclerView (SelectionTracker) -> [ArtistsFragment], [ArtistAdapter], [ArtistItemKeyProvider], [ArtistItemDetailsLookup]    
  ```SelectionTracker``` is another to do ```setOnClickListener```, which is more powerful while multiple items need selecting   
- BottomNavigationView -> [UIComponentsActivity]    
- TabLayout + ViewPager -> [MusicFragment], [UIComponentsActivity]    
- SearchView -> [SearchSongsActivity], [AndroidManifest]     

### SearchView
Believe if or not, a SearchView could be far more complicated than you've expected.   
Before we get started, let's take a look at some features of SearchView:   
- Search Interface    
- Query suggestion (either recent searches or custom suggestions)    
- Query history   
- Searchable configuration (E.g. voice search)    

[Read more](https://developer.android.com/guide/topics/search)    

First thing first, you need to create either Search Dialog (a SearchView inside NavigationView) or Search Widget (your custom search view, which could be an EditText placed anywhere in your layout).     

Secondly, think about how you handle the search view. Here is my workflow:        
1. Start the ```SearchSongsActivity```       
2. User types something in ```SearchView``` and press enter        
3. Start the ```SearchSongsActivity``` again        
4. Handle searches in ```onNewIntent()```      

You need the following:      
1. Add tags to your result activity in AndroidManifest.xml
```xml
<activity
    android:name=".activities.SearchSongsActivity"
    android:launchMode="singleTop">

    <intent-filter>
        <action android:name="android.intent.action.SEARCH" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>

    <intent-filter>
        <action android:name="com.google.android.gms.actions.SEARCH_ACTION" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>

     <meta-data
        android:name="android.app.searchable"
        android:resource="@xml/searchable" />
</activity>
```

2. In xml/searchable.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<searchable xmlns:android="http://schemas.android.com/apk/res/android"
    android:hint="@string/search_hint"
    android:label="@string/app_name"
    android:searchSuggestAuthority="com.catherine.materialdesignapp.providers.SearchSuggestionProvider"
    android:searchSuggestSelection=" ?"
    android:voiceSearchMode="showVoiceSearchButton|launchRecognizer" />
```

> ```android:searchSuggestAuthority"```: (Optional) Refer to your search content provider     
> ```android:searchSuggestSelection"```: (Optional) Pop up search suggestions, " ?" means query    
> ```android:voiceSearchMode"```: (Optional) To enable voice search    
> Read more search configuration here: https://developer.android.com/guide/topics/search/searchable-config       


(Optional) Create your search suggestion content provider       
```java
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.catherine.materialdesignapp.providers.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
```

And register your content provider in AndroidManifest.xml
```xml
<provider
    android:name=".providers.SearchSuggestionProvider"
    android:authorities="com.catherine.materialdesignapp.providers.SearchSuggestionProvider" />
```

3. Handle intents and create a search icon in your activity
```java
public class SearchSongsActivity extends AppCompatActivity {  
    private final static String TAG = SearchSongsActivity.class.getSimpleName();  
    private SearchManager searchManager;
    private SearchView searchView;

  /**
     * In this case, this onNewIntent will be called while
     * user finishes searching, this activity will be relaunch.
     * <p>
     * Because
     * 1. this activity launches in single top/task/instance mode
     * 2. ACTION_SEARCH is defined in intent-filter
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null)
            return;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            // Handle the scenario that user submitted searches:
            // 1. Fill in what user just typed in SearchView automatically
            // 2. Dismiss search suggestions
            // 3. Query
            // 4. Save queries
            searchView.setQuery(query, false);
            searchView.clearFocus();
            query(query);
            saveQueries(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.searchable_menu, menu);

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:

                // clear query history
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void query(String text) {
        // do something
    }

    // save queries while you've defined a search content provider.
    private void saveQueries(String text) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        suggestions.saveRecentQuery(text, null);
    }
}
```

4. In menu/ui_components_menu.xml    
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/action_search"
        android:actionLayout="@layout/searchview_layout"
        android:icon="@drawable/ic_search_black_24dp"
        android:title="@string/action_search"
        app:actionViewClass="androidx.appcompat.widget.SearchView"
        app:showAsAction="ifRoom|collapseActionView" />
</menu>
```
> ```app:showAsAction="collapseActionView"```: Click the search icon and stretch the view   
> ```setIconified(false)```: Always show the search field       


## Custom Layouts
Build a custom view from scratch:       
1. [Slider](https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/components/Slider.java)        
2. [attrs](https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/res/values/attrs.xml)      
3. [BackgroundServiceFragment], [fragment_background_service](https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/res/layout/fragment_background_service.xml)         


# Localisation
List all resource directories you should take care of:       
1. animator/     
2. anim/     
3. color/        
4. drawable/     
5. mipmap/       
6. layout/       
7. menu/     
8. raw/      
9. values/       
10. xml/     
11. font/       


# Device compatibility
1. Create alternative UI resources such as layouts, drawables and mipmaps     
2. Set layout files with the following rules:       
    - Avoid hard-coded layout sizes by using ```wrap_content```, ```match_parent``` and ```layout_weight```, etc        
    - Prefer ```ConstraintLayout```     
    - Redraw views when window configuration changes (multi-window mode or screen rotation)        
3. Define alternative layouts for specific screen sizes. E.g. ```layout-w600dp``` and ```layout-w600dp-land``` for 7” tablets and 7” tablets in landscape representative        
4. Create stretchable nine-patch bitmaps        
5. Build a dynamic UI with fragments        
6. Test on all screen sizes     

ConstraintLayout example: []      
Fragments example: []     

[Read more](https://developer.android.com/training/multiscreen/screensizes)


## Pixel densities
Pixel density is how many pixels within a physical area of the screen, ```dpi``` is the basic unit.       

**dpi**: Dots per inch      
**resolution**: The total number of pixels on a screen      
**dp or dip**: Instead of px (pixel), measure UI with dp (density-independent pixels) on mobile devices       


## UI Spec
|               | ldpi     | mdpi     | hdpi     | xhdpi    | xxhdpi     | xxxhdpi    |
|---------------|----------|----------|----------|----------|------------|------------|
| Scaling ratio | 0.75x    | 1x       | 1.5x     | 2x       | 3x         | 4x         |
| Dpi           | ~120dpi  | ~160dpi  | ~240dpi  | 320dpi   | 480dpi     | 640dip     |
| App icon size | 36x36 px | 48x48 px | 72x72 px | 96x96 px | 144x144 px | 192x192 px |

> nodpi: bitmaps in nodpi drawables look larger in xhdpi devices whereas it seems smaller on mdpi devices.      
> anydpi: These bitmaps in anydpi have priority when no bitmaps are found in other drawable directories. For instance, we have ```drawable-hdpi/banner.9.png``` and ```drawable-anydpi/banner.xml```, ```banner.9.png``` will be used on hdpi devices and ```banner.xml``` will be seen on other devices.       

To see more details by automatically importing icons with Android Studio Image Asset tools and have a look at [Grid and keyline shapes]        
![screenshot](https://raw.githubusercontent.com/Catherine22/AAD-Preparation/master/AAD-Preparation/screenshots/image-asset.png)  

```res``` directory example: [res]     

[Read more](https://developer.android.com/guide/practices/screens_support)        


# Lifecycle
[Read more](https://developer.android.com/guide/components/activities/activity-lifecycle)


## Saving UI states
1. ```ViewModel```      
2. ```onSaveInstanceState()```      
To test ```savedInstanceState```, have ```Do not keep activities``` selected on system Settings page to test ```onSaveInstanceState``` and ```onRestoreInstanceState```       
Code: [LifecycleActivity]       
3. Persistent in local storage for complex or large data       

[Read more](https://developer.android.com/topic/libraries/architecture/saving-states.html)     


## Monitor lifecycle events via ```Lifecycle``` class in two ways:        
1. Implement both ```LifecycleObserver``` and ```LifecycleOwner```      
Code: [LifecycleActivity], [LifecycleObserverImpl]

2. Associate with Jetpack       

[Read more](https://developer.android.com/topic/libraries/architecture/lifecycle.html#java)

## Handle configuration changes     
1. Enable activities to handle configuration changes like screen rotation and keyboard availability change
```xml
<activity android:name=".MyActivity"
          android:configChanges="orientation|keyboardHidden"
          android:label="@string/app_name">
```

2. In activities        
```java
 @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // do something
    }
```
Code: [BaseActivity]        
[Read more](https://blog.csdn.net/zhaokaiqiang1992/article/details/19921703)        

# App components
There are four different types of app components:       
- Activity      
- Services      
- Broadcast receivers       
- Content providers     

[Read more](https://developer.android.com/guide/components/fundamentals)

## Activity

### 4 Launch Modes
```xml
<activity
    android:name=".SingleTaskActivity"
    android:launchMode="singleTask">
```

Let's say we have 4 activities: A, B, C and D       

1. standard     
- Default mode, it pushes new activities on the top of the stack.     
- Example (how activities work in the stack): (bottom) A-B-B-D-A-C-C (top)      

2. singleTop        
- No duplicate activities on the top, but there could be same activities in the stack.     
- Example (how activities work in the stack): (bottom) A-B-A-C-D-C (top)        
- Let's say C is the top activity, and you try to launch C again. Then this C won't be created, instead, ```onNewIntent()``` will be called in existed C.       

3. singleTask
- No duplicate activities in the stack.      
- Example (how activities work in the stack): (bottom) A-B-C-D (top)        
- Let's say C is in the stack, and you try to launch C again. Then this C won't be created, instead, ```onNewIntent()``` will be called in existed C.       
- Don't forget to set taskAffinity (```android:taskAffinity="your packageName"```) to map the activity to app.

4. singleInstance
- ```singleTask``` + ```taskAffinity``` is more recommended.
- No duplicate activities in the system, which means if there is A running in App1, then we lunch A in App2, this running A in App1 will be killed.       
- Example (how activities work in the stack): (bottom) A-B-C-D (top)        
- Let's say C is in the stack, and you try to launch C again. Then this C won't be created, instead, ```onNewIntent()``` will be called in existed C.       
- You could set taskAffinity (```android:taskAffinity="your packageName"```) as well


NOTICE: You might need to handle both ```onCreate()``` and ```onNewIntent()``` lifecycle events in ```singleTop```

## Fragment
1. To build a multi-pane UI   
2. To reuse fragments in multiple activities.   
3. A fragment must always be hosted in an activity and the fragment's lifecycle is directly affected by the host activity's lifecycle.    


Code (Activity + Fragment): [UIComponentsActivity]   
Code (ViewPager + Fragment): [MusicFragment]    
[Read more](https://developer.android.com/guide/components/fragments)

## Services
- Two services in Android - background service and foreground service     
- Background services are no longer working since Android Oreo, you are suppose to use ```JobScheduler``` instead.      
- Foreground services and JobScheduler are alternatives to run app in the background, but ```notification``` is required while running a foreground service.      
- ```JobScheduler``` is only available on devices running API 21+. Fore devices running API 14+ including devices without Google Play serivces, ```WorkManager``` let you schedule background tasks that need guaranteed completion,  whether or not the app process is running.

| Android API level | background service | foreground service | job scheduler |
| -- | -- | -- | -- |
| ≤ 25 | O | X | X |
| ≥ 26 | X | O | O |


### ```JobScheduler``` TIPS:
1. Because setting this property is not compatible with persisted jobs, doing so will throw an IllegalArgumentException when ```JobInfo.Builder.build()``` is called.   
2. ```jobScheduler.cancel(JOB_ID)``` or ```jobScheduler.cancelAll()``` only works while jobs haven't started. For example, a job is scheduled to start in 5 seconds (```setMinimumLatency(5000)```), ```cancel()``` works right before the job actually runs.    
3. Don't forget to finish jobs if the task is done. (```jobFinished(jobParameters, false)```)

Code: [BackgroundServiceFragment], [MusicPlayerService], [MusicPlayerJobScheduler], [AndroidManifest]       
[Read more](https://developer.android.com/guide/components/services)        

## Broadcast receiver
You could either register receivers by dynamically extending ```BroadcastReceiver``` or statically declaring an implementation with the ```<receiver>``` tag in the AndroidManifest.xml

Code: [NetworkHealthService], [NetworkHealthJobScheduler], [InternetConnectivityReceiver]        
[Read more](https://developer.android.com/reference/android/content/BroadcastReceiver)       

## Content Provider
Create your own content providers to share data with other applications or access existing content providers in another applications.       

### System content providers
**In order to get the uri path, we are going to have a look at android source code.**       
1. Go to https://android.googlesource.com/platform/packages/providers/ and pick out needed providers        
2. Search ```<provider>``` tag in AndroidManifest, e.g. in https://android.googlesource.com/platform/packages/providers/UserDictionaryProvider/+/refs/tags/android-9.0.0_r33/AndroidManifest.xml        
```xml
<provider android:name="CallLogProvider"
            android:authorities="call_log"
            android:syncable="false" android:multiprocess="false"
            android:exported="true"
            android:readPermission="android.permission.READ_CALL_LOG"
            android:writePermission="android.permission.WRITE_CALL_LOG">
        </provider>
```
3. Now we have host name (```android:authorities```), then, go to [CallLogProvider] (```android:name```) to get the table name        
4. Search "urimatcher" in [UserDictionaryProvider], and have a bunch of ```sURIMatcher.addURI()``` found. We figure out that one url is "content://call_log/calls"        
5. Grant permission: ```<uses-permission android:name="android.permission.READ_CALL_LOG" />```, ```<uses-permission android:name="android.permission.WRITE_CALL_LOG" />```

**CRUD - Create**

**CRUD - Read**

**CRUD - Update**

**CRUD - Delete**

### User-defined content providers
A content provider uri should be ```scheme + authority + table + [id] + [filter]```. E.g. ```content://com.catherine.myapp/member/1/name```     

Code: [ContentProviderFragment]

# Working in the background
Tasks on a background thread using ```AsyncTask``` (for short or interruptible tasks) or ```AsyncTaskLoader``` (for tasks that are high-priority, or tasks that need to report back to the user or UI).   

## AsyncTask
- run on UI thread: ```onPreExecute```, ```onProgressUpdate``` and ```onPostExecute```    
- update progress to UI via ```publishProgress```, handle data in ```onProgressUpdate```   
- ```WeakReference```   
- ```executeOnExecutor```      

Code: [BackgroundActivity], [SleepTask]   

## AsyncTaskLoader    
When you want the data to be available even if the device configuration changes, use ```loaders```    
This ```getLoaderManager()``` or ```getSupportLoaderManager()``` is deprecated since Android P. Instead, we use ```ViewModels``` and ```LiveData```   

- Call ```getLoaderManager()``` or ```getSupportLoaderManager()``` depends on whether you use ```Support Library```   
- ```initLoader``` is supposed to be called in ```onCreate()```   
- Call ```restartLoader``` is equivalent to ```initLoader```, but ```initLoader``` only works at the first time.    
- ```onLoadFinished``` is run on background thread, this may cause memory leak if updating UI here - use at your own risk.    

Code: [BackgroundActivity], [SleepTaskLoader]   

## ViewModels and LiveData

# Notification
Three style of notifications:   
1. Standard notification    
2. Notification with actions (one or two buttons)   
3. Replying notification

## Notification dots (Badge)
Long click app icons on Android O+ devices, notification badge will pop up.    

## Notification Channels
Classify notifications by channels in the Settings app on Android O+ devices.    


Code: [NotificationActivity]    
[Read more](https://codelabs.developers.google.com/codelabs/android-training-notifications/index.html?index=..%2F..android-training#0)

# Accessibility Features
1. Set ContentDescription       
2. Make the views focusable     


# Day/Night Mode
1. Define your own style in styles.xml, notice your style must extend whatever styles contain ".DayNight" keyword.      
```xml
<style name="AppTheme.NoActionBar" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="windowActionBar">false</item>
    <item name="windowNoTitle">true</item>
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```
or
```xml
<style name="AppTheme" parent="AppTheme.DayNight">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```

2.
In Manifest, update the theme
```xml
android:theme="@style/AppTheme.NoActionBar"
```

3. Initialise night mode programmatically if you want
```java
public class MainActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initNightMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // We set the theme, immediately in the Activity’s onCreate()
    private void initNightMode() {
        Storage storage = new Storage(this);
        int nightMode = storage.retrieveInt(Storage.NIGHT_MODE);
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
```

4. Switch day/night mode programmatically if you want
```java
SharedPreferences sharedPreferences = getSharedPreferences("main", Context.MODE_PRIVATE);
button.setOnClickListener(
    v -> {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            button.setTitle(getString(R.string.action_night_mode));
            sharedPreferences.edit().putInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO).apply();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            item.setTitle(getString(R.string.action_day_mode));
            sharedPreferences.edit().putInt("night_mode", AppCompatDelegate.MODE_NIGHT_YES).apply();
        }
        // Recreate the activity for the theme change to take effect.
        recreate();
    }
);
```

Code: [MainActivity]
[Read more](https://www.youtube.com/watch?v=1by5J7c5Vz4)       

# Styles
To inherit styles from Android support library by using ```parent```
```xml
<style name="GreenText" parent="TextAppearance.AppCompat">
    <item name="android:textColor">#00FF00</item>
</style>
```

And to inherit user-defined styles, you could use a dot notation
```xml
<style name="GreenText.Large">
    <item name="android:textSize">22dp</item>
</style>
```

Code: [styles](https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/res/values/styles.xml)      


# Jetpack
[Video](https://www.youtube.com/watch?v=7p22cSzniBM)        
[Guide](https://developer.android.com/jetpack/docs/guide)

## Room
A robust SQLite object mapping library. 

## WorkManager
WorkManager providers APIs for deferrable, one-off and recurring background tasks that need guaranteed execution 

## ViewModel
ViewModel is constructed when app configuration changes such as screen rotation

## LiveData
LiveData is an observable data holder for the data is meant to be shown on screens.

## Paging
Paging library integrates directly with endless data

## Navigation
Navigation library simplifies implementation of complex but common navigation requirements.


Go to [google tutorial](https://codelabs.developers.google.com/codelabs/android-training-livedata-viewmodel/index.html?index=..%2F..android-training#0) to see how ```Room``` + ```ViewModel``` + ```LiveData``` works.     

Code(Room + ViewModel + LiveData): [PlaylistFragment], [AlbumsFragment], [ArtistsFragment] and [jetpack](https://github.com/Catherine22/AAD-Preparation/tree/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/jetpack)      


## Migrate to AndroidX
1. In order to activate AndroidX, add two flags in gradle.properties    
```
android.useAndroidX=true
android.enableJetifier=true
```

2. Remove android support libraries ```com.android.support...``` and ```android.arch...```, all the changes must be implemented to both classes and layouts.    

3. Update test options in build.gradle    
```gradle
android {
  defaultConfig {
          testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
      }
  testOptions {
          execution 'ANDROIDX_TEST_ORCHESTRATOR'
      }
}

dependencies {
    androidTestImplementation 'androidx.test:runner:1.1.1'
    androidTestUtil 'androidx.test:orchestrator:1.1.1'
}
```


[Read More](https://developer.android.com/jetpack/androidx/migrate)

# Kotlin
The exam is only available in Java at this time (4/1/2019)      
[Read more](https://kotlinlang.org/docs/reference/)     

1. [Basic Types]    
    - Bitwise operators     
    - == vs ===     
    - Numbers       
    - Characters      
    - Strings         
    - Array     
2. [Control Flow]     
    - ```when```      
    - ```if```        
    - ```while```       
3. [Returns and Jumps]     
    - ```break```       
    - ```xxx@ for``` or ```xxx@ while```     
    - ```return```      
4. [Classes and Inheritance]      
    - Class with multiple constructors      
    - Inheritance       
    - ```interface```     
    - ```override```      
    - ```inner class```     
    - ```super@xxx.f()```       
    - ```abstract class```      
5. [Properties and Fields]       
    - getter and setter      
    - ```lateinit```        
    - ```::```      
6. [Visibility Modifiers]     
    - ```open```        
    - ```public```      
    - ```internal```        
    - ```protected```       
    - ```private```     
7. [BaseClass], [BaseClassExtensions]       
    - Extension functions       
    - Extension properties      
    - Companion objects (which is similar to ```static```)     
    - Call extension functions of the base class declared other class       
    - Call functions both declared in the base class and self class inside extension functions (check [BaseClassExtensions])      
8. [Data Class]     
    - Properties declared in the primary constructor or class body      
    - Copying    
    - E.g. [User], [Employee]      
9. [Sealed Class], [Enum Classes]             
    - enum vs sealed class      
10. [Generics]      
    - Declaration-site variance     
    - Type projections      
11. [Nested and Inner Classes]      
    - Nested Class      
    - Inner Class       
    - Anonymous Inner Class [NOT YET]     
12. [Enum classes]      
    - Basic usage of enum classes       
    - Another way to initialise the enum     
    - Enum constants can also declare their own anonymous classes       
    - Print all values of enum class        
13. [Objects]       
    - Object expressions - class        
    - Object expressions - class + interface        
    - Anonymous objects     
    - Object declarations (Singleton)       
    - Companion objects     
14. [Type aliases]      
    - Shorten types declaration     
15. [Inline classes]      
    - An inline class must have a single property initialised in the primary constructor        
    - Inline classes cannot have properties with backing fields, ie, your code would be:     
    ```Kotlin
    val length: Int
      get() = s.length
    ```
    - Representation: Inline classes could be as inline, generic, interface or nullable   
    - Inline classes vs type aliases    
    - Enable inline classes in Gradle:    
    ```Gradle
    compileKotlin {
        kotlinOptions.freeCompilerArgs += ["-XXLanguage:+InlineClasses"]
    }
    ```
16. [Delegation]    
  - Implementation by delegation    
  - ```by```    
  ```Kotlin
  // The last "b" (from ": Base by b") is implemented by the "b" in "Derived(b: Base)"
  class Derived(b: Base) : Base by b
  ```
  - Overriding functions and variables is optional    
17. [Delegated Properties]    
  - Declare standard Delegates(```Lazy```, ```Observable``` and storing properties in a Map) via ```by```
  - Implement properties including standard delegates once for all    
  - Local delegated properties    
  -     
18. [Kotlin Singleton] vs [Java Singleton]    
19. [Functions]   
  - Basic functions   
  - Functions with default arguments    
  - Override functions    
  - Lambda    
  - variable number of arguments (```vararg```)    
  - Unit-returning functions    
  - ```infix fun```       
20. Projections   
  - in-projections    
  - out-projections   
  - star-projections    
21. [Higher-Order Functions and Lambdas]    
  - Higher-Order Functions is a function that takes functions as parameters, or returns a function.   
  - Compare callbacks in Java and Kotlin (SAM for Kotlin classes)    
  - Lambda functions    
  - Pass functions as arguments to another function (```this::func```)    
  - Passing a lambda to the last parameter    
  - Implement a function type as an interface (You can either override ```invoke()``` or run ```invoke()```)   
  - ```invoke()```    
  - ```::```    
22. Inline Functions    




[Material design]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#material-design>
[Localisation]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#Localisation>
[Device compatibility]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#device-compatibility>
[res]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/res/>
[Lifecycle]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#lifecycle>
[App components]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#app-components>
[Working in the background]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#working-in-the-background>
[Notification]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#notification>
[Accessibility Features]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#accessibility-features>
[Day/Night Mode]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#daynight-mode>
[Styles]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#styles>
[Jetpack]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#jetpack>
[Kotlin]:<https://github.com/Catherine22/AAD-Preparation/blob/master/README.md#kotlin>

[BaseActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/BaseActivity.java>
[MainActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/MainActivity.java>
[LifecycleActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/LifecycleActivity.java>
[LifecycleObserverImpl]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/utils/LifecycleObserverImpl.java>
[AppComponentsActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/AppComponentsActivity.java>
[MusicPlayerService]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/services/MusicPlayerService.java>
[MusicPlayerJobScheduler]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/services/MusicPlayerJobScheduler.java>
[AndroidManifest]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/AndroidManifest.xml>
[InternetConnectivityReceiver]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/receivers/InternetConnectivityReceiver.java>
[NetworkHealthService]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/services/NetworkHealthService.java>
[NetworkHealthJobScheduler]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/services/NetworkHealthJobScheduler.java>
[BackgroundActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/BackgroundActivity.java>
[SleepTask]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/tasks/SleepTask.java>
[SleepTaskLoader]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/tasks/SleepTaskLoader.java>
[NotificationActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/NotificationActivity.java>
[ContentProviderFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/ContentProviderFragment.java>
[BackgroundServiceFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/BackgroundServiceFragment.java>
[rv_album_item]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/res/layout/rv_album_item.xml>
[PlaylistAdapter]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/adapters/PlaylistAdapter.java>
[PlaylistFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/PlaylistFragment.java>
[AlbumsFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/AlbumsFragment.java>
[ArtistsFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/ArtistsFragment.java>
[ArtistAdapter]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/adapters/ArtistAdapter.java>
[ArtistItemKeyProvider]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/components/ArtistItemKeyProvider.java>
[ArtistItemDetailsLookup]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/components/ArtistItemDetailsLookup.java>
[RecyclerViewItemTouchHelper]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/components/RecyclerViewItemTouchHelper.java>
[MusicFragment]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/fragments/MusicFragment.java>
[UIComponentsActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/UIComponentsActivity.java>
[SearchSongsActivity]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/java/com/catherine/materialdesignapp/activities/SearchSongsActivity.java>
[AndroidManifest]:<https://github.com/Catherine22/AAD-Preparation/blob/master/AAD-Preparation/app/src/main/AndroidManifest.xml>


[Basic Types]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/BasicTypes.kt>
[Control Flow]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/ControlFlow.kt>
[Returns and Jumps]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/ReturnsAndJumps.kt>
[Classes and Inheritance]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/MyClass.kt>
[Properties and Fields]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/PropertiesAndFields.kt>
[Visibility Modifiers]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/package1/VisibilityModifiers.kt>
[BaseClass]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/BaseClass.kt>
[BaseClassExtensions]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/package1/BaseClassExtensions.kt>
[Data Class]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/DataClass.kt>
[User]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/User.kt>
[Employee]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/Employee.kt>
[Nested and Inner Classes]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/InnerClassExample.kt>
[Sealed Class]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/SealedClass.kt>
[Enum Classes]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/EnumClasses.kt>
[Objects]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/Objects.kt>
[Type aliases]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/TypeAliases.kt>
[Inline classes]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/InlineClass.kt>
[Delegation]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/Delegation.kt>
[Delegated Properties]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/DelegatedProperties.kt>
[Kotlin Singleton]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/singleton_kotlin/>
[Java Singleton]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/singleton_java/>
[Functions]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/Functions.kt>
[Higher-Order Functions and Lambdas]:<https://github.com/Catherine22/AAD-Preparation/blob/master/KotlinFromScratch/src/Lambdas.kt>

[CallLogProvider]:<https://android.googlesource.com/platform/packages/providers/ContactsProvider/+/refs/tags/android-9.0.0_r34/src/com/android/providers/contacts/CallLogProvider.java>
[Grid and keyline shapes]:<https://material.io/design/iconography/#grid-keyline-shapes>
