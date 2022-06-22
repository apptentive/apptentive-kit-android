FROM gradle:7-jdk11-openj9 as build

LABEL maintainer="Apptentive Engineering <engineering@apptentive.com>"

# Install Android SDK
# https://developer.android.com/studio#command-tools
ARG SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip"
ARG ANDROID_HOME="/usr/local/android-sdk"

RUN mkdir -p "$ANDROID_HOME" /root/.android \
  && touch /root/.android/repositories.cfg

WORKDIR $ANDROID_HOME

RUN curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"

RUN apt-get update \
    && apt-get install -y build-essential file apt-utils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME=${ANDROID_HOME}

# Project Specific
WORKDIR /app

COPY . .

RUN ./gradlew tasks
