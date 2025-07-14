#!/bin/bash

gomobile bind -androidapi 21 -o ../android-app/app/libs/mixin.aar -trimpath -ldflags "-s -w" -target=android/arm,android/arm64,android/amd64 mixin/tip mixin/jwt mixin/blockchain mixin/ed25519 mixin/kernel
rm ../android-app/app/libs/mixin-sources.jar
