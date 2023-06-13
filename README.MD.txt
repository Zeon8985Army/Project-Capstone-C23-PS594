# Requiredment:
1. JDK 17
 1.1 Make sure you assign JAVA_HOME path to location of JDK 17
2. Gradle 7.2
3. Android Studio 2021.1.1 Path 1 (Please use this version, because We Create this app usi this version, it will effect all dependencies that we use)

All Dependencies (Already exist in Gradle):
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.6.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.firebase:firebase-auth-ktx:21.0.4'
    implementation 'com.google.firebase:firebase-firestore-ktx:24.1.2'
    implementation 'com.google.firebase:firebase-storage-ktx:20.0.1'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
    // Import the BoM for the Firebase platform
    implementation platform('com.google.firebase:firebase-bom:30.1.0')

    // Declare the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation 'com.google.firebase:firebase-auth-ktx'

    // Also declare the dependency for the Google Play services library and specify its version
    implementation 'com.google.android.gms:play-services-auth:20.2.0'

    // Untuk Image Load
    implementation 'com.squareup.picasso:picasso:2.71828'

    // Image API
    implementation 'com.squareup.okhttp3:okhttp:4.9.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0'

Don't forget to sync or create gradle, if it is not working, please rebuild the project or make sure requerement already exist