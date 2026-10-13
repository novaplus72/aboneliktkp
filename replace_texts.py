import re
import os

files = [
    "app/src/main/java/com/example/ui/screens/SettingsScreen.kt",
    "app/src/main/java/com/example/ui/screens/DashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt",
    "app/src/main/java/com/example/ui/SubscriptionViewModel.kt",
    "app/src/main/java/com/example/ui/screens/OnboardingScreen.kt"
]

texts = [
    "Abonelik Ekle", "Aylık Toplam Ödeme", "Aylık bütçenizi aştınız!", "Limit Yok", "Yaklaşan Ödemeler",
    "Tüm Abonelikler", "Yeni Ekle", "Bugün", "Kategori", "Tutar", "Ödeme Günü (1-31)", "Açıklama (Opsiyonel)",
    "Abonelik Adı", "Ekle", "Düzenle", "Güncelle", "Sil", "Dil", "Uygulama dilini seçin", "Karşılama Ekranı",
    "Hoş Geldiniz", "Uygulamayı kullanmaya başlamadan önce temel ayarları yapın.", "Başla", "Cihaz Dili",
    "Bütçe Kullanımı: ", "Tüm abonelikleriniz silinecektir. Devam etmek istediğinizden emin misiniz?", "Sıfırla",
    "WebDAV Sunucu Ayarları", "Sunucu URL (örn: https://dav.box.com/dav)", "Kullanıcı Adı", "Şifre / Uygulama Şifresi",
    "Dosya Yolu", "Bağlantıyı Test Et", "Bağlanıyor...", "WebDAV üzerinden indirilecek olan abonelik verileri mevcut listenize eklenecektir. Devam etmek istiyor musunuz?",
    "Geri Yüklemeyi Onayla", "Aboneliği Sil", "aboneliğinizi silmek istediğinizden emin misiniz?", "Evet, Sil",
    "Abonelik Adı (örn: Netflix)", "Aylık", "Yıllık", "Gelecek Ödeme: ", "Açıklama / Notlar (İsteğe bağlı)",
    "Ödeme Hatırlatıcısı", "Vazgeç", "Ayarlar", "Arayüz ve Genel Ayarlar", "Koyu Tema", "Koyu renk şemasını aktif et",
    "Varsayılan Para Birimi", "Aylık Harcama Limiti", "Bildirim ve Hatırlatıcılar", "Hatırlatıcı Bildirimler",
    "Ödeme günleri yaklaştığında uyar", "Kaç Gün Önce Uyarılsın?", "Bildirim Sistemini Test Et", "Örnek bir hatırlatıcı gönder",
    "Bulut ve Yerel Senkronizasyon", "Cihaza Yedekle (Yerel)", "Abonelikleri telefonunuza JSON olarak kaydedin",
    "Cihazdan Geri Yükle (Yerel)", "Kayıtlı JSON yedeğini telefondan yükleyin", "WebDAV (Bulut) Yapılandırması",
    "Sunucu:", "Dosya:", "Senkronizasyon:", "Durum:", "Ayarlanmadı", "Yedekle", "Geri Yükle", "İşlem yapılıyor, lütfen bekleyin...",
    "Raporlama ve Paylaşım", "Raporu Dışa Aktar", "Abonelik verilerini paylaş", "Tehlikeli Bölge", "Tüm Verileri Temizle",
    "Abonelikleri kalıcı olarak sil", "Kaydet", "İptal", "Gün", "Değiştirmek için tıklayın", "Aylık olarak aşmak istemediğiniz toplam abonelik bütçenizi girin. İptal etmek için boş bırakın veya 0 yazın.",
    "Varsayılan Para Birimi Seçin", "Kapat", "Bildirim Eşiğini Belirleyin", "Sıfırlama İşlemini Onayla",
    "Panel", "Analiz", "Bildirim izni verildi! Hatırlatıcılar aktif.", "Bildirim izni reddedildi. Ödeme uyarıları gönderilemeyecek.",
    "Yaklaşan Ödeme: ", "ödemeniz çok yakında! Ödeme Tutarı: ", "Test Bildirimi", "Bildirim sistemi sorunsuz çalışıyor!",
    "WebDAV sunucu adresi girilmemiş.", "Başarılı", "Hata", "Geçersiz yedekleme dosyası formatı.", "Dosya okunamadı.",
    "Yerel yedekleme başarıyla tamamlandı! ", "Yerel geri yükleme başarılı! ", " abonelik aktarıldı.", "Gün Önce",
    "Abonelikleriniz Excel / CSV formatında hazırlanıyor...", "Abonelik Raporu", "Rapor Dışa Aktarıldı!", "Rapor Paylaşımı",
    "Rapor verisi boş!"
]

# We need to be careful with exact string matches in Text("...") or inside Toasts.
def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # We add import com.example.util.Localization.getString if not exists
    if "import com.example.util.Localization.getString" not in content and "package " in content:
        content = content.replace("import androidx", "import com.example.util.Localization.getString\nimport androidx", 1)

    for text in texts:
        # Replace Text("exact_text") with Text(getString("exact_text"))
        content = re.sub(rf'Text\("{text}"\)', rf'Text(getString("{text}"))', content)
        content = re.sub(rf'Text\("{text}",', rf'Text(getString("{text}"),', content)
        content = re.sub(rf'text = "{text}"', rf'text = getString("{text}")', content)
        content = re.sub(rf'label = {{ Text\("{text}"\)', rf'label = {{ Text(getString("{text}"))', content)
        content = re.sub(rf'placeholder = {{ Text\("{text}"\)', rf'placeholder = {{ Text(getString("{text}"))', content)
        content = re.sub(rf'title = "{text}"', rf'title = getString("{text}")', content)
        content = re.sub(rf'subtitle = "{text}"', rf'subtitle = getString("{text}")', content)
        content = re.sub(rf'description = "{text}"', rf'description = getString("{text}")', content)
        content = re.sub(rf'Toast.makeText\(context, "{text}"', rf'Toast.makeText(context, getString("{text}")', content)

    # Some variables inside strings
    # "Yaklaşan Ödeme: ${sub.name}" -> getString("Yaklaşan Ödeme: ") + sub.name
    # "${sub.name} ödemeniz çok yakında! Ödeme Tutarı: $formattedAmount" -> sub.name + getString("ödemeniz çok yakında! Ödeme Tutarı: ") + formattedAmount
    
    with open(filepath, 'w') as f:
        f.write(content)

for file in files:
    if os.path.exists(file):
        process_file(file)

