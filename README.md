# Media-Player

Simple Android media player app that scans device storage for videos and audios using MediaStore.

## Features
- Tabs for Videos, Audios, and custom Albums
- Media lists with name, size, duration, and play button
- TikTok-style vertical video feed with smooth swipe between videos
- Create custom in-app video albums and add videos to albums
- Video playback using `VideoView`
- Audio playback using `MediaPlayer`
- Seek bar, time display, play/pause/stop controls, and volume slider (audio player)
- Runtime permission handling (`READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE` for older Android)

## Build
```bash
./gradlew assembleDebug
```

APK output (default): `app/build/outputs/apk/debug/app-debug.apk`
