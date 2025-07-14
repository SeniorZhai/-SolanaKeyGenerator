#!/bin/bash
gomobile bind -target=ios,iossimulator/arm64 -o ./TIP.xcframework -trimpath -ldflags "-s -w" mixin/tip mixin/kernel mixin/blockchain mixin/ed25519
