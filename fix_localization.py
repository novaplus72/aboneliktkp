import re
with open('app/src/main/java/com/example/util/Localization.kt', 'r') as f:
    lines = f.readlines()

new_en_map = {
    "Abonelik Ekle": "Add Subscription",
    "Aylık Toplam Ödeme": "Total Monthly Payment",
    "Aylık bütçenizi aştınız!": "You exceeded your monthly budget!",
    "Limit Yok": "No Limit",
    "Yaklaşan Ödemeler": "Upcoming Payments",
    "Tüm Abonelikler": "All Subscriptions",
    "Yeni Ekle": "Add New",
    "Bugün": "Today",
    "Kategori": "Category",
    "Tutar": "Amount",
    "Ödeme Günü (1-31)": "Payment Day (1-31)",
    "Açıklama (Opsiyonel)": "Description (Optional)",
    "Abonelik Adı": "Subscription Name",
    "Ekle": "Add",
    "Düzenle": "Edit",
    "Güncelle": "Update",
    "Sil": "Delete",
    "Dil": "Language",
    "Uygulama dilini seçin": "Select application language",
    "Karşılama Ekranı": "Welcome",
    "Hoş Geldiniz": "Welcome",
    "Uygulamayı kullanmaya başlamadan önce temel ayarları yapın.": "Configure basic settings before using the app.",
    "Başla": "Start",
    "Cihaz Dili": "Device Language",
    "Bütçe Kullanımı: ": "Budget Usage: ",
    "Tüm abonelikleriniz silinecektir. Devam etmek istediğinizden emin misiniz?": "All your subscriptions will be deleted. Are you sure you want to continue?",
    "Sıfırla": "Reset",
    "WebDAV Sunucu Ayarları": "WebDAV Server Settings",
    "Sunucu URL (örn: https://dav.box.com/dav)": "Server URL (e.g., https://dav.box.com/dav)",
    "Kullanıcı Adı": "Username",
    "Şifre / Uygulama Şifresi": "Password / App Password",
    "Dosya Yolu": "File Path",
    "Bağlantıyı Test Et": "Test Connection",
    "Bağlanıyor...": "Connecting...",
    "WebDAV üzerinden indirilecek olan abonelik verileri mevcut listenize eklenecektir. Devam etmek istiyor musunuz?": "Subscription data downloaded via WebDAV will be added to your current list. Do you want to continue?",
    "Geri Yüklemeyi Onayla": "Confirm Restore",
    "Aboneliği Sil": "Delete Subscription",
    "aboneliğinizi silmek istediğinizden emin misiniz?": "are you sure you want to delete your subscription?",
    "Evet, Sil": "Yes, Delete",
    "Abonelik Adı (örn: Netflix)": "Subscription Name (e.g., Netflix)",
    "Aylık": "Monthly",
    "Yıllık": "Yearly",
    "Gelecek Ödeme: ": "Next Payment: ",
    "Açıklama / Notlar (İsteğe bağlı)": "Description / Notes (Optional)",
    "Ödeme Hatırlatıcısı": "Payment Reminder",
    "Vazgeç": "Cancel",
    "Ayarlar": "Settings",
    "Arayüz ve Genel Ayarlar": "Interface & General Settings",
    "Koyu Tema": "Dark Theme",
    "Koyu renk şemasını aktif et": "Enable dark color scheme",
    "Varsayılan Para Birimi": "Default Currency",
    "Aylık Harcama Limiti": "Monthly Expense Limit",
    "Bildirim ve Hatırlatıcılar": "Notifications & Reminders",
    "Hatırlatıcı Bildirimler": "Reminder Notifications",
    "Ödeme günleri yaklaştığında uyar": "Warn when payment days approach",
    "Kaç Gün Önce Uyarılsın?": "How Many Days Before?",
    "Bildirim Sistemini Test Et": "Test Notification System",
    "Örnek bir hatırlatıcı gönder": "Send a sample reminder",
    "Bulut ve Yerel Senkronizasyon": "Cloud & Local Sync",
    "Cihaza Yedekle (Yerel)": "Backup to Device (Local)",
    "Abonelikleri telefonunuza JSON olarak kaydedin": "Save subscriptions to your phone as JSON",
    "Cihazdan Geri Yükle (Yerel)": "Restore from Device (Local)",
    "Kayıtlı JSON yedeğini telefondan yükleyin": "Load saved JSON backup from phone",
    "WebDAV (Bulut) Yapılandırması": "WebDAV (Cloud) Configuration",
    "Sunucu:": "Server:",
    "Dosya:": "File:",
    "Senkronizasyon:": "Sync:",
    "Durum:": "Status:",
    "Ayarlanmadı": "Not Set",
    "Yedekle": "Backup",
    "Geri Yükle": "Restore",
    "İşlem yapılıyor, lütfen bekleyin...": "Processing, please wait...",
    "Raporlama ve Paylaşım": "Reporting & Sharing",
    "Raporu Dışa Aktar": "Export Report",
    "Abonelik verilerini paylaş": "Share subscription data",
    "Tehlikeli Bölge": "Danger Zone",
    "Tüm Verileri Temizle": "Clear All Data",
    "Abonelikleri kalıcı olarak sil": "Permanently delete subscriptions",
    "Kaydet": "Save",
    "İptal": "Cancel",
    "Gün": "Days",
    "Değiştirmek için tıklayın": "Click to change",
    "Aylık olarak aşmak istemediğiniz toplam abonelik bütçenizi girin. İptal etmek için boş bırakın veya 0 yazın.": "Enter your monthly subscription budget. Leave empty or enter 0 to cancel.",
    "Tutar": "Amount",
    "Varsayılan Para Birimi Seçin": "Select Default Currency",
    "Kapat": "Close",
    "Bildirim Eşiğini Belirleyin": "Set Notification Threshold",
    "Sıfırlama İşlemini Onayla": "Confirm Reset",
    "Panel": "Dashboard",
    "Analiz": "Analytics",
    "Bildirim izni verildi! Hatırlatıcılar aktif.": "Notification permission granted! Reminders are active.",
    "Bildirim izni reddedildi. Ödeme uyarıları gönderilemeyecek.": "Notification permission denied. Payment alerts will not be sent.",
    "Yaklaşan Ödeme: ": "Upcoming Payment: ",
    "ödemeniz çok yakında! Ödeme Tutarı: ": "payment is coming up soon! Payment Amount: ",
    "Test Bildirimi": "Test Notification",
    "Bildirim sistemi sorunsuz çalışıyor!": "Notification system is working properly!",
    "WebDAV sunucu adresi girilmemiş.": "WebDAV server address not entered.",
    "Başarılı": "Success",
    "Hata": "Error",
    "Geçersiz yedekleme dosyası formatı.": "Invalid backup file format.",
    "Dosya okunamadı.": "File could not be read.",
    "Yerel yedekleme başarıyla tamamlandı! ": "Local backup completed successfully! ",
    "Yerel geri yükleme başarılı! ": "Local restore successful! ",
    " abonelik aktarıldı.": " subscriptions transferred.",
    "Gün Önce": "Days Before",
    "Abonelikleriniz Excel / CSV formatında hazırlanıyor...": "Your subscriptions are being prepared in Excel / CSV format...",
    "Abonelik Raporu": "Subscription Report",
    "Rapor Dışa Aktarıldı!": "Report Exported!",
    "Rapor Paylaşımı": "Report Sharing",
    "Rapor verisi boş!": "Report data is empty!"
}

new_de_map = {
    "Abonelik Ekle": "Abonnement hinzufügen",
    "Aylık Toplam Ödeme": "Monatliche Gesamtzahlung",
    "Aylık bütçenizi aştınız!": "Sie haben Ihr monatliches Budget überschritten!",
    "Limit Yok": "Kein Limit",
    "Yaklaşan Ödemeler": "Anstehende Zahlungen",
    "Tüm Abonelikler": "Alle Abonnements",
    "Yeni Ekle": "Neu hinzufügen",
    "Bugün": "Heute",
    "Kategori": "Kategorie",
    "Tutar": "Betrag",
    "Ödeme Günü (1-31)": "Zahlungstag (1-31)",
    "Açıklama (Opsiyonel)": "Beschreibung (Optional)",
    "Abonelik Adı": "Abonnement-Name",
    "Ekle": "Hinzufügen",
    "Düzenle": "Bearbeiten",
    "Güncelle": "Aktualisieren",
    "Sil": "Löschen",
    "Dil": "Sprache",
    "Uygulama dilini seçin": "App-Sprache auswählen",
    "Karşılama Ekranı": "Willkommen",
    "Hoş Geldiniz": "Willkommen",
    "Uygulamayı kullanmaya başlamadan önce temel ayarları yapın.": "Nehmen Sie Grundeinstellungen vor, bevor Sie die App nutzen.",
    "Başla": "Starten",
    "Cihaz Dili": "Gerätesprache",
    "Bütçe Kullanımı: ": "Budgetnutzung: ",
    "Tüm abonelikleriniz silinecektir. Devam etmek istediğinizden emin misiniz?": "Alle Ihre Abonnements werden gelöscht. Sind Sie sicher, dass Sie fortfahren möchten?",
    "Sıfırla": "Zurücksetzen",
    "WebDAV Sunucu Ayarları": "WebDAV Server-Einstellungen",
    "Sunucu URL (örn: https://dav.box.com/dav)": "Server-URL (z. B. https://dav.box.com/dav)",
    "Kullanıcı Adı": "Benutzername",
    "Şifre / Uygulama Şifresi": "Passwort / App-Passwort",
    "Dosya Yolu": "Dateipfad",
    "Bağlantıyı Test Et": "Verbindung testen",
    "Bağlanıyor...": "Verbindung wird hergestellt...",
    "WebDAV üzerinden indirilecek olan abonelik verileri mevcut listenize eklenecektir. Devam etmek istiyor musunuz?": "Über WebDAV heruntergeladene Abonnementdaten werden zu Ihrer aktuellen Liste hinzugefügt. Möchten Sie fortfahren?",
    "Geri Yüklemeyi Onayla": "Wiederherstellung bestätigen",
    "Aboneliği Sil": "Abonnement löschen",
    "aboneliğinizi silmek istediğinizden emin misiniz?": "sind Sie sicher, dass Sie Ihr Abonnement löschen möchten?",
    "Evet, Sil": "Ja, löschen",
    "Abonelik Adı (örn: Netflix)": "Abonnement-Name (z.B. Netflix)",
    "Aylık": "Monatlich",
    "Yıllık": "Jährlich",
    "Gelecek Ödeme: ": "Nächste Zahlung: ",
    "Açıklama / Notlar (İsteğe bağlı)": "Beschreibung / Notizen (Optional)",
    "Ödeme Hatırlatıcısı": "Zahlungserinnerung",
    "Vazgeç": "Abbrechen",
    "Ayarlar": "Einstellungen",
    "Arayüz ve Genel Ayarlar": "Oberfläche & Allgemeine Einstellungen",
    "Koyu Tema": "Dunkles Design",
    "Koyu renk şemasını aktif et": "Dunkles Farbschema aktivieren",
    "Varsayılan Para Birimi": "Standardwährung",
    "Aylık Harcama Limiti": "Monatliches Ausgabenlimit",
    "Bildirim ve Hatırlatıcılar": "Benachrichtigungen & Erinnerungen",
    "Hatırlatıcı Bildirimler": "Erinnerungsbenachrichtigungen",
    "Ödeme günleri yaklaştığında uyar": "Warnen, wenn Zahlungstage näher rücken",
    "Kaç Gün Önce Uyarılsın?": "Wie viele Tage vorher?",
    "Bildirim Sistemini Test Et": "Benachrichtigungssystem testen",
    "Örnek bir hatırlatıcı gönder": "Beispielerinnerung senden",
    "Bulut ve Yerel Senkronizasyon": "Cloud- & lokale Synchronisierung",
    "Cihaza Yedekle (Yerel)": "Auf Gerät sichern (Lokal)",
    "Abonelikleri telefonunuza JSON olarak kaydedin": "Abonnements als JSON auf Telefon speichern",
    "Cihazdan Geri Yükle (Yerel)": "Vom Gerät wiederherstellen (Lokal)",
    "Kayıtlı JSON yedeğini telefondan yükleyin": "Gespeichertes JSON-Backup vom Telefon laden",
    "WebDAV (Bulut) Yapılandırması": "WebDAV (Cloud) Konfiguration",
    "Sunucu:": "Server:",
    "Dosya:": "Datei:",
    "Senkronizasyon:": "Sync:",
    "Durum:": "Status:",
    "Ayarlanmadı": "Nicht festgelegt",
    "Yedekle": "Sichern",
    "Geri Yükle": "Wiederherstellen",
    "İşlem yapılıyor, lütfen bekleyin...": "Wird bearbeitet, bitte warten...",
    "Raporlama ve Paylaşım": "Berichte & Freigabe",
    "Raporu Dışa Aktar": "Bericht exportieren",
    "Abonelik verilerini paylaş": "Abonnementdaten teilen",
    "Tehlikeli Bölge": "Gefahrenzone",
    "Tüm Verileri Temizle": "Alle Daten löschen",
    "Abonelikleri kalıcı olarak sil": "Abonnements dauerhaft löschen",
    "Kaydet": "Speichern",
    "İptal": "Abbrechen",
    "Gün": "Tage",
    "Değiştirmek için tıklayın": "Zum Ändern klicken",
    "Aylık olarak aşmak istemediğiniz toplam abonelik bütçenizi girin. İptal etmek için boş bırakın veya 0 yazın.": "Geben Sie Ihr monatliches Abonnement-Budget ein. Leer lassen oder 0 eingeben, um abzubrechen.",
    "Tutar": "Betrag",
    "Varsayılan Para Birimi Seçin": "Standardwährung auswählen",
    "Kapat": "Schließen",
    "Bildirim Eşiğini Belirleyin": "Benachrichtigungsschwelle festlegen",
    "Sıfırlama İşlemini Onayla": "Zurücksetzen bestätigen",
    "Panel": "Dashboard",
    "Analiz": "Analytik",
    "Bildirim izni verildi! Hatırlatıcılar aktif.": "Benachrichtigungsberechtigung erteilt! Erinnerungen sind aktiv.",
    "Bildirim izni reddedildi. Ödeme uyarıları gönderilemeyecek.": "Benachrichtigungsberechtigung verweigert. Zahlungsbenachrichtigungen werden nicht gesendet.",
    "Yaklaşan Ödeme: ": "Anstehende Zahlung: ",
    "ödemeniz çok yakında! Ödeme Tutarı: ": "Zahlung steht bald an! Zahlungsbetrag: ",
    "Test Bildirimi": "Test-Benachrichtigung",
    "Bildirim sistemi sorunsuz çalışıyor!": "Benachrichtigungssystem funktioniert einwandfrei!",
    "WebDAV sunucu adresi girilmemiş.": "WebDAV-Serveradresse nicht eingegeben.",
    "Başarılı": "Erfolg",
    "Hata": "Fehler",
    "Geçersiz yedekleme dosyası formatı.": "Ungültiges Backup-Dateiformat.",
    "Dosya okunamadı.": "Datei konnte nicht gelesen werden.",
    "Yerel yedekleme başarıyla tamamlandı! ": "Lokales Backup erfolgreich abgeschlossen! ",
    "Yerel geri yükleme başarılı! ": "Lokale Wiederherstellung erfolgreich! ",
    " abonelik aktarıldı.": " Abonnements übertragen.",
    "Gün Önce": "Tage Vorher",
    "Abonelikleriniz Excel / CSV formatında hazırlanıyor...": "Ihre Abonnements werden im Excel / CSV-Format vorbereitet...",
    "Abonelik Raporu": "Abonnementbericht",
    "Rapor Dışa Aktarıldı!": "Bericht exportiert!",
    "Rapor Paylaşımı": "Bericht freigeben",
    "Rapor verisi boş!": "Berichtsdaten sind leer!"
}

# we rebuild Localization.kt
with open('app/src/main/java/com/example/util/Localization.kt', 'w') as f:
    f.write('''package com.example.util

import java.util.Locale

object Localization {
    var currentLanguage = "tr" // tr, en, de

    fun setLanguage(lang: String) {
        if (lang == "device") {
            currentLanguage = when (Locale.getDefault().language) {
                "tr" -> "tr"
                "de" -> "de"
                else -> "en"
            }
        } else {
            currentLanguage = lang
        }
    }

''')

    f.write('    private val enMap = mapOf(\n')
    en_items = [f'        "{k}" to "{v}"' for k,v in new_en_map.items()]
    f.write(',\n'.join(en_items))
    f.write('\n    )\n\n')

    f.write('    private val deMap = mapOf(\n')
    de_items = [f'        "{k}" to "{v}"' for k,v in new_de_map.items()]
    f.write(',\n'.join(de_items))
    f.write('\n    )\n\n')

    f.write('''    fun getString(trText: String): String {
        return when (currentLanguage) {
            "en" -> enMap[trText] ?: trText
            "de" -> deMap[trText] ?: trText
            else -> trText
        }
    }
}
''')
