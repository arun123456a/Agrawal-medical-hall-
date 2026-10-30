AGRAWAL MEDICAL - शुरुआती Android ऐप

इसमें दवाई का नाम, बैच नंबर, मात्रा और एक्सपायरी तारीख सेव होती है।
रोजाना रिमाइंडर में 90, 30, 7 दिन पहले और एक्सपायर हो चुकी दवाइयों की सूचना आती है।
तारीख YYYY-MM-DD में लिखें, उदाहरण: 2027-05-31।

महत्वपूर्ण:
- यह शुरुआती संस्करण एक मोबाइल में स्थानीय रूप से डेटा रखता है।
- ऐप अनइंस्टॉल/फोन बदलने पर डेटा मिट सकता है; अभी बैकअप/क्लाउड सिंक नहीं है।
- कर्मचारी के कई फोन पर साझा डेटा और OTP लॉगिन अभी शामिल नहीं हैं।
- Android में नोटिफिकेशन की अनुमति दें। फोन की बैटरी/ऐप प्रतिबंध रिमाइंडर रोक सकते हैं।
- यह सोर्स कोड है, APK नहीं। GitHub Actions से APK बनेगी।


FIREBASE CONFIGURATION (30 Oct 2026)
- google-services.json is included in app/google-services.json.
- Google Services Gradle plugin and Firebase Auth/Firestore libraries are configured.
- This configuration alone does NOT implement OTP login, employee roles, or cloud sync in the app UI.
- Enable Phone provider in Firebase Authentication and create Firestore database before testing those features.
