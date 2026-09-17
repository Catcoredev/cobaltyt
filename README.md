# Cobalt YT

Android YouTube client with:

- light theme
- search
- history
- video player
- offline downloads
- no endless Shorts scrolling
- Cobalt API integration

## Important API note

Cobalt's current documentation says hosted instances such as `api.cobalt.tools` are protected and are not intended for third-party projects without explicit permission. This project therefore expects **your own Cobalt API instance** or an instance whose owner has explicitly given you access.

Official Cobalt:
https://github.com/imputnet/cobalt

Cobalt API docs:
https://github.com/imputnet/cobalt/blob/main/docs/api.md

## 1. Run your own Cobalt API

The official Cobalt documentation recommends Docker Compose for a normal deployment.

For local development, the documented requirements include Node.js >= 18, git and pnpm. The API can be started from `cobalt/api` after configuring `.env`.

Example local API URL:

http://localhost:9000/

On the Android Emulator, `localhost` means the emulator itself. Use:

http://10.0.2.2:9000/

For a physical Android phone, use the LAN IP of the computer hosting Cobalt, for example:

http://192.168.1.50:9000/

For an internet-facing deployment, use HTTPS and protect the instance with authentication/reverse proxy as recommended by Cobalt.

## 2. YouTube search

Cobalt is used for resolving media URLs. It is not a YouTube search API.

Cobalt YT therefore uses YouTube Data API v3 for search. Put your API key into:

Settings -> YouTube Data API v3 key

The key is stored locally in the app's private preferences.

## 3. Open in Android Studio

1. Install Android Studio.
2. Open the `CobaltYT` folder.
3. Let Gradle sync.
4. Use JDK 17.
5. Connect an Android phone or start an emulator.
6. Run the `app` configuration.

The project uses AndroidX Media3/ExoPlayer for playback. Media3 supports both internet streams and local media files.

## 4. Configure

Settings:

Cobalt API URL:
- Emulator + local Cobalt: http://10.0.2.2:9000/
- Physical phone + PC on same LAN: http://YOUR-PC-LAN-IP:9000/
- Remote server: https://YOUR-COBALT-DOMAIN/

YouTube Data API v3 key:
- enter your own key

## 5. Offline

Press the download button beside a search result.

Cobalt YT asks the Cobalt instance for a media URL and hands that URL to Android Download Manager.

Files are saved under:

Downloads/Cobalt YT/

The app does not provide an endless download feed. Download only content you have permission to save and use.

## 6. Current limitations

- YouTube search requires a YouTube Data API v3 key.
- Cobalt `picker` responses are not yet implemented.
- Cobalt `local-processing` responses are not yet implemented; configure the instance to return tunnel/redirect output.
- Offline library UI currently points to Android Downloads; a future version can index local files inside the app.
- History is local to this device.
- The app deliberately has no Shorts auto-scroll feed.

## License

The Cobalt project is AGPL-3.0. Check the Cobalt repository and its component licenses before redistributing a combined project.
