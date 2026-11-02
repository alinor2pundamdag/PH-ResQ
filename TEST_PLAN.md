# Android device test plan (NOT YET RUN)

Run only on a spare test device with a test contact number. Never send a false emergency request to 911.

1. Build debug APK using the README instructions. Record device model, Android version, build hash, installation result and crash logs.
2. First launch: read the privacy screen; decline and confirm offline guidance remains available. Return and accept, restart and confirm persistence. Open all dashboard tiles; unsupported features must show honest unavailable states, never fabricated weather or disaster alerts. Open offline guidance and the dialer. Do **not** place a call to 911.
3. With no contact, try SOS hold and accessible alternative: both must block SMS composition. Enter an invalid number and confirm it is not saved; save a test number, restart and confirm it persists; clear and confirm deletion.
4. Deny location permission: confirm no crash, no coordinates in draft SMS. Grant location and inspect displayed timestamp and accuracy; if unavailable, confirm omission. Wait over five minutes and confirm stale coordinates are omitted.
5. Tap SOS briefly and cancel: SMS app must not launch. Hold over two seconds, then cancel confirmation: SMS app must not launch. Use the accessible alternative and confirm correct recipient and message. Cancel from SMS app: no claim of delivery. Repeat without SIM or SMS app and inspect fallback message.
6. Save an incident note, restart and confirm persistence, then delete it. In Settings delete local data and confirm first-launch screen returns; verify contact and note are cleared. Toggle airplane mode and battery saver; repeat, observing app never claims successful message delivery or live GPS. Test large font, TalkBack, rotation, process kill and relaunch.

**Release blocker:** this checklist is not evidence of passing tests. The project lacks validated nationwide data, maps, encrypted vault, actual mesh transport, verified weather feeds, integration tests and independent safety review.

7. With consent granted, connect and refresh USGS observations; verify source/time labels. Disconnect and refresh: old cache remains labeled with its original timestamp. Delete local data: cache and scheduled refresh are removed. Do not interpret observations as PHIVOLCS warnings.
