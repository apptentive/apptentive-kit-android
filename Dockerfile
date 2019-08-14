FROM gradle:5.5 AS deps

LABEL maintainer="Brett McGinnis <brett.mcginnis@apptentive.com>"

ARG SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip"
ARG ANDROID_HOME="/usr/local/android-sdk"

# Install Android SDK
RUN mkdir "$ANDROID_HOME" .android

WORKDIR $ANDROID_HOME

RUN curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"

ARG ANDROID_VERSION=28
ARG ANDROID_BUILD_TOOLS_VERSION=28.0.3

RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"

RUN apt-get update \
    && apt-get install -y build-essential file apt-utils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Project Specific
WORKDIR /app

COPY . .

RUN ./gradlew tasks