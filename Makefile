PACKAGE = de.cineaste.android
APK = app/build/outputs/apk/app-debug.apk
APKRELEASE = app/build/outputs/apk/app-release.apk

all: debug install start

clean: release uninstall installRelease start

debug:
	./gradlew assembleDebug

lint:
	./gradlew lintDebug

release:
	@./gradlew assembleRelease \
		-Pandroid.injected.signing.store.file=$(ANDROID_KEYFILE) \
		-Pandroid.injected.signing.store.password=$(ANDROID_STORE_PASSWORD) \
		-Pandroid.injected.signing.key.alias=$(ANDROID_KEY_ALIAS) \
		-Pandroid.injected.signing.key.password=$(ANDROID_KEY_PASSWORD)

install:
	adb $(TARGET) install -rk $(APK)

installRelease:
	 adb $(TARGET) install -rk $(APKRELEASE)
start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean