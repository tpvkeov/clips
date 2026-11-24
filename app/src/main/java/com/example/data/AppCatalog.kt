package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.AvatarAccessory
import com.example.model.AvatarPreset
import com.example.model.CameraFilter

/**
 * Static UI catalogs only (avatar styles, camera filters, categories).
 * No accounts, numbers, videos, posts, or stories live here anymore — all of
 * that comes from Supabase at runtime.
 */
object AppCatalog {

    /** Reserved id used when the user uploads a real photo from the device. */
    const val CUSTOM_AVATAR_ID = "av_custom"

    val avatarPresets: List<AvatarPreset> = listOf(
        AvatarPreset(
            id = "av_creator",
            nameAr = "المخرج المحترف",
            categoryAr = "ثلاثي الأبعاد",
            primaryColor = Color(0xFF1F2937),
            secondaryColor = Color(0xFFFF3B5C),
            accentColor = Color(0xFF00E5FF),
            accessory = AvatarAccessory.HEADPHONES,
            useCreatorPhoto = true,
            imageUrl = "https://api.dicebear.com/9.x/avataaars/png?seed=AhmedDirector&size=256&backgroundColor=1f2937,ff7a1a&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_cyber",
            nameAr = "سايبر نيون",
            categoryAr = "مستقبلي",
            primaryColor = Color(0xFF0F172A),
            secondaryColor = Color(0xFF00E5FF),
            accentColor = Color(0xFFD946EF),
            accessory = AvatarAccessory.CYBER_VISOR,
            imageUrl = "https://api.dicebear.com/9.x/bottts/png?seed=CyberNeon99&size=256&backgroundColor=0f172a,00e5ff&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_crown",
            nameAr = "الملك الذهبي",
            categoryAr = "مميز",
            primaryColor = Color(0xFF291D0A),
            secondaryColor = Color(0xFFF59E0B),
            accentColor = Color(0xFFFDE047),
            accessory = AvatarAccessory.CROWN,
            imageUrl = "https://api.dicebear.com/9.x/adventurer/png?seed=GoldenKing&size=256&backgroundColor=291d0a,f59e0b&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_lens",
            nameAr = "عدسة السينما",
            categoryAr = "تصوير",
            primaryColor = Color(0xFF111827),
            secondaryColor = Color(0xFF3B82F6),
            accentColor = Color(0xFF60A5FA),
            accessory = AvatarAccessory.CAMERA_LENS,
            imageUrl = "https://api.dicebear.com/9.x/micah/png?seed=CinemaLens&size=256&backgroundColor=111827,3b82f6&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_cool",
            nameAr = "مظهر كلاسيكي",
            categoryAr = "لايف ستايل",
            primaryColor = Color(0xFF1E1B4B),
            secondaryColor = Color(0xFF818CF8),
            accentColor = Color(0xFFF472B6),
            accessory = AvatarAccessory.SUNGLASSES,
            imageUrl = "https://api.dicebear.com/9.x/personas/png?seed=StarClassic&size=256&backgroundColor=1e1b4b,818cf8&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_halo",
            nameAr = "هالة الإبداع",
            categoryAr = "فني",
            primaryColor = Color(0xFF064E3B),
            secondaryColor = Color(0xFF10B981),
            accentColor = Color(0xFFA7F3D0),
            accessory = AvatarAccessory.STAR_HALO,
            imageUrl = "https://api.dicebear.com/9.x/lorelei/png?seed=CreativeHalo&size=256&backgroundColor=064e3b,10b981&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_astro",
            nameAr = "رائد الفضاء",
            categoryAr = "خيال",
            primaryColor = Color(0xFF0C1B33),
            secondaryColor = Color(0xFF38BDF8),
            accentColor = Color(0xFFE0F2FE),
            accessory = AvatarAccessory.ASTRONAUT,
            imageUrl = "https://api.dicebear.com/9.x/notionists/png?seed=AstroTravel&size=256&backgroundColor=0c1b33,38bdf8&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_fire",
            nameAr = "طاقة اللهب",
            categoryAr = "ألعاب",
            primaryColor = Color(0xFF3F0D12),
            secondaryColor = Color(0xFFFF4D4D),
            accentColor = Color(0xFFFFB703),
            accessory = AvatarAccessory.FIRE_AURA,
            imageUrl = "https://api.dicebear.com/9.x/avataaars/png?seed=FireAuraPro&size=256&backgroundColor=3f0d12,ff4d4d&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_mask",
            nameAr = "قناع الظل",
            categoryAr = "غموض",
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFFA855F7),
            accentColor = Color(0xFF22D3EE),
            accessory = AvatarAccessory.NEON_MASK,
            imageUrl = "https://api.dicebear.com/9.x/micah/png?seed=ShadowMask&size=256&backgroundColor=18181b,a855f7&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_badge",
            nameAr = "صانع محتوى مميز",
            categoryAr = "صانع محتوى",
            primaryColor = Color(0xFF1E293B),
            secondaryColor = Color(0xFFEC4899),
            accentColor = Color(0xFFFDE047),
            accessory = AvatarAccessory.CREATOR_BADGE,
            imageUrl = "https://api.dicebear.com/9.x/lorelei/png?seed=BayanStar&size=256&backgroundColor=1e293b,ec4899&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_music",
            nameAr = "إيقاع الصوت",
            categoryAr = "موسيقى",
            primaryColor = Color(0xFF31102F),
            secondaryColor = Color(0xFFF43F5E),
            accentColor = Color(0xFF2DD4BF),
            accessory = AvatarAccessory.MUSIC_NOTES,
            imageUrl = "https://api.dicebear.com/9.x/adventurer/png?seed=SoundBeat&size=256&backgroundColor=31102f,f43f5e&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_bolt",
            nameAr = "برق التحدي",
            categoryAr = "رياضة",
            primaryColor = Color(0xFF0F292F),
            secondaryColor = Color(0xFF06B6D4),
            accentColor = Color(0xFFFACC15),
            accessory = AvatarAccessory.LIGHTNING,
            imageUrl = "https://api.dicebear.com/9.x/big-smile/png?seed=BoltSport&size=256&backgroundColor=0f292f,06b6d4&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_gamer",
            nameAr = "محترف الألعاب",
            categoryAr = "ألعاب",
            primaryColor = Color(0xFF1F1638),
            secondaryColor = Color(0xFF8B5CF6),
            accentColor = Color(0xFF34D399),
            accessory = AvatarAccessory.GAMER_EARS,
            imageUrl = "https://api.dicebear.com/9.x/bottts/png?seed=ProGamerX&size=256&backgroundColor=1f1638,8b5cf6&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_diamond",
            nameAr = "الألماس الأزرق",
            categoryAr = "فاخر",
            primaryColor = Color(0xFF0B2545),
            secondaryColor = Color(0xFF60A5FA),
            accentColor = Color(0xFFE0F2FE),
            accessory = AvatarAccessory.DIAMOND,
            imageUrl = "https://api.dicebear.com/9.x/personas/png?seed=BlueDiamond&size=256&backgroundColor=0b2545,60a5fa&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_director",
            nameAr = "كلاكيت ستوديو",
            categoryAr = "سينما",
            primaryColor = Color(0xFF1C1917),
            secondaryColor = Color(0xFFEF4444),
            accentColor = Color(0xFFFBBF24),
            accessory = AvatarAccessory.DIRECTOR_CAP,
            imageUrl = "https://api.dicebear.com/9.x/notionists/png?seed=ClapboardDirector&size=256&backgroundColor=1c1917,ef4444&backgroundType=gradientLinear"
        ),
        AvatarPreset(
            id = "av_ring",
            nameAr = "إضاءة رينج لايت",
            categoryAr = "بث مباشر",
            primaryColor = Color(0xFF172554),
            secondaryColor = Color(0xFF22D3EE),
            accentColor = Color(0xFFF472B6),
            accessory = AvatarAccessory.STUDIO_RING,
            imageUrl = "https://api.dicebear.com/9.x/big-smile/png?seed=RingLightLive&size=256&backgroundColor=172554,22d3ee&backgroundType=gradientLinear"
        )
    )

    val cameraFilters: List<CameraFilter> = listOf(
        CameraFilter("normal", "طبيعي", Color.Transparent, 0f),
        CameraFilter("cinema", "سينمائي", Color(0xFF0F2C4C), 0.24f),
        CameraFilter("neon", "نيون وردي", Color(0xFFE11D48), 0.18f),
        CameraFilter("golden", "ساعة ذهبية", Color(0xFFF59E0B), 0.20f),
        CameraFilter("cyber", "سايبر سماوي", Color(0xFF00E5FF), 0.18f),
        CameraFilter("emerald", "تباين زمردي", Color(0xFF059669), 0.18f)
    )

    val categories: List<String> = listOf("الكل", "تقنية", "سفر", "ألعاب", "تصميم", "طبخ")
}
