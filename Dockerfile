FROM gradle:5.5 AS build

LABEL maintainer="Brett McGinnis <brett.mcginnis@apptentive.com>"

# Install Android SDK
ARG SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip"
ARG ANDROID_HOME="/usr/local/android-sdk"

RUN mkdir -p "$ANDROID_HOME" /root/.android \
  && touch /root/.android/repositories.cfg

WORKDIR $ANDROID_HOME

RUN curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"

ARG ANDROID_VERSION=29
ARG ANDROID_BUILD_TOOLS_VERSION=29.0.1

RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"

RUN apt-get update \
    && apt-get install -y build-essential file apt-utils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME=${ANDROID_HOME}

# Project Specific
WORKDIR /app

COPY . .

RUN ./gradlew tasks
