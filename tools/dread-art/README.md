# Updating the models

Run `python bake.py` in this directory with Python 3 and Pillow. Copy the resulting enemy JSONs from `models/` to the project’s `models/rigs/`, their .bbmodel files to the matching `blockbench/` faction directory, and the textures from the generated resource overlay to `src/main/resources/assets/destiny2mc/textures/entity/`. The standard project build regenerates the native geometry from the JSON. Animation source and mixin registrations are already integrated under `src/main/`.

The accompanying art notes describe the reference silhouettes, pivots and attack timing. Their standalone workshop layout refers to the output of the authoring script.
