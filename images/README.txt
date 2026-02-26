====================================================
  JAWA BIKE SHOWROOM — LOCAL IMAGE FOLDER
====================================================

Place your bike images here with the following filenames:

  JW001.jpg  →  Jawa 42 Standard
  JW002.jpg  →  Jawa 42 ABS
  JW003.jpg  →  Jawa Perak Bobber
  JW004.jpg  →  Jawa 300 Scrambler
  JW005.jpg  →  Jawa 42 FJ
  JW006.jpg  →  Jawa 350

SUPPORTED FORMATS: .jpg / .jpeg / .png

HOW THE APP FINDS IMAGES:
  The app looks for images relative to the working directory
  (i.e., the project root folder — same level as src/, build.sh, etc.)
  So the full expected path is:   images/JW001.jpg   (etc.)

  If you run the app from a different directory, either:
  - Change to the project root before running, OR
  - Use absolute paths in BikeListPanel.java BIKE_IMAGE_PATHS map

TIPS:
  - Landscape images (wider than tall) look best in the cards.
  - Recommended resolution: 640x360 or larger.
  - If an image file is missing, the app will show a painted silhouette
    fallback — no error, no crash.
====================================================
