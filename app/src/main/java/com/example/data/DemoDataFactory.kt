package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.AvatarAccessory
import com.example.model.AvatarPreset
import com.example.model.CameraFilter
import com.example.model.CommentItem
import com.example.model.FeatureSuggestion
import com.example.model.MediaVisualStyle
import com.example.model.SuggestionComment
import com.example.model.PostItem
import com.example.model.StorySlide
import com.example.model.UserProfile
import com.example.model.UserStoryGroup
import com.example.model.VideoItem
import com.example.model.VideoOrientation

object DemoDataFactory {

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

    val initialUserProfile = UserProfile(
        name = "أحمد المنصور",
        handle = "@ahmed_studio",
        bio = "صانع محتوى مرئي ومخرج أفلام قصيرة • شغوف بالتصوير السينمائي والمونتاج وتطوير تجارب التصميم الحديثة.",
        followersCount = "148K",
        followingCount = "312",
        totalLikesCount = "1.9M",
        avatarPresetId = "av_creator",
        points = 18
    )

    val categories: List<String> = listOf("الكل", "تقنية", "سفر", "ألعاب", "تصميم", "طبخ")

    // Direct HTTPS MP4 samples verified from the browser (HTTP 200/206, video/mp4).
    // Keeping them in one place means reels, home cards, posts and stories all use
    // genuinely playable video instead of synthetic artwork.
    private object DemoMediaUrls {
        const val FLOWER = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4"
        const val SINTEL_1080P = "https://download.blender.org/durian/trailer/sintel_trailer-1080p.mp4"
        const val SINTEL_480P = "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4"
        const val W3_SINTEL = "https://media.w3.org/2010/05/sintel/trailer.mp4"
        const val W3_BUNNY = "https://media.w3.org/2010/05/bunny/trailer.mp4"
        const val W3_BUNNY_MOVIE = "https://media.w3.org/2010/05/bunny/movie.mp4"
        const val W3_MOVIE_300 = "https://media.w3.org/2010/05/video/movie_300.mp4"
        const val BBB_1MB = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4"
        const val BBB_2MB = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_2MB.mp4"
        const val JELLYFISH_1MB = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_1MB.mp4"
        const val SINTEL_1MB = "https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_1MB.mp4"
        const val SAMPLE_640 = "https://filesamples.com/samples/video/mp4/sample_640x360.mp4"
    }

    val initialHorizontalVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "h1",
            title = "جولة داخل استوديو التصوير الاحترافي وإعدادات الإضاءة السينمائية 4K",
            description = "نشرح في هذا الفيديو خطوات بناء استوديو تصوير منزلي بإضاءة سينمائية متوازنة وتوزيع الميكروفونات.",
            creatorName = "أحمد المنصور",
            creatorHandle = "@ahmed_studio",
            creatorAvatarId = "av_creator",
            duration = "14:28",
            viewsCount = 128400,
            likesCount = 9420,
            sharesCount = 640,
            isSaved = true,
            orientation = VideoOrientation.HORIZONTAL,
            category = "تقنية",
            timeAgo = "منذ ساعتين",
            visualStyle = MediaVisualStyle.STUDIO_PHOTO,
            // Original 1080p source for the home card and linked post preview.
            mediaUriString = DemoMediaUrls.SINTEL_1080P,
            isMine = true,
            comments = listOf(
                CommentItem("c1", "كريم عادل", "av_lens", "الإضاءة الجانبية أعطت عمق خرافي للمشهد!", "منذ ساعة"),
                CommentItem("c2", "سارة محمود", "av_halo", "أفضل شرح لإعداد الألوان والمونتاج.", "منذ 35 دقيقة")
            )
        ),
        VideoItem(
            id = "h2",
            mediaUriString = DemoMediaUrls.W3_SINTEL,
            title = "رحلة ليلية في قلب المدينة المستقبلية — تصوير سينمائي بالعدسة العريضة",
            description = "لقطات ليلية ساحرة بين الأضواء والانعكاسات المطرية باستخدام عدسة 24mm.",
            creatorName = "نور الإبداع",
            creatorHandle = "@noor_visuals",
            creatorAvatarId = "av_cyber",
            duration = "09:15",
            viewsCount = 84200,
            likesCount = 6150,
            sharesCount = 410,
            orientation = VideoOrientation.HORIZONTAL,
            category = "سفر",
            timeAgo = "منذ 5 ساعات",
            visualStyle = MediaVisualStyle.TRAVEL_SUNSET,
            comments = listOf(
                CommentItem("c3", "طارق حسن", "av_astro", "الألوان خيالية، استمر!", "منذ 3 ساعات")
            )
        ),
        VideoItem(
            id = "h3",
            mediaUriString = DemoMediaUrls.FLOWER,
            title = "تحدي تصميم واجهات وتجربة مستخدم كاملة في أقل من ساعة",
            description = "نظرة تفصيلية على بناء الهوية البصرية واختيار الألوان التفاعلية لتطبيقات الجوال.",
            creatorName = "استوديو بيان",
            creatorHandle = "@bayan_design",
            creatorAvatarId = "av_badge",
            duration = "21:04",
            viewsCount = 56900,
            likesCount = 4830,
            sharesCount = 320,
            orientation = VideoOrientation.HORIZONTAL,
            category = "تصميم",
            timeAgo = "منذ يوم",
            visualStyle = MediaVisualStyle.DESIGN_GRID,
            comments = listOf(
                CommentItem("c4", "ليلى سامي", "av_diamond", "التدرجات اللونية مريحة جداً للعين.", "منذ 12 ساعة")
            )
        ),
        VideoItem(
            id = "h4",
            mediaUriString = DemoMediaUrls.BBB_1MB,
            title = "مراجعة أقوى أجهزة الألعاب المحمولة وتجربة الأداء الفعلي على 120 إطار",
            description = "اختبار شامل للحرارة، البطارية، وجودة الشاشة مع أحدث الألعاب الثقيلة.",
            creatorName = "جيمر زون",
            creatorHandle = "@gamer_zone",
            creatorAvatarId = "av_gamer",
            duration = "16:40",
            viewsCount = 215000,
            likesCount = 15200,
            sharesCount = 1120,
            orientation = VideoOrientation.HORIZONTAL,
            category = "ألعاب",
            timeAgo = "منذ يومين",
            visualStyle = MediaVisualStyle.GAMING_NEON
        ),
        VideoItem(
            id = "h5",
            mediaUriString = DemoMediaUrls.SAMPLE_640,
            title = "كيف تسجّل بودكاست بصوت نقي وجودة إذاعية بأبسط الأدوات",
            description = "دليل عملي لاختيار الميكروفون وعزل الصوت ومعالجة النقاء الصوتي.",
            creatorName = "أحمد المنصور",
            creatorHandle = "@ahmed_studio",
            creatorAvatarId = "av_creator",
            duration = "11:52",
            viewsCount = 43700,
            likesCount = 3190,
            sharesCount = 215,
            orientation = VideoOrientation.HORIZONTAL,
            category = "تقنية",
            timeAgo = "منذ 3 أيام",
            visualStyle = MediaVisualStyle.PODCAST_MIC,
            isMine = true
        ),
        VideoItem(
            id = "h6",
            mediaUriString = DemoMediaUrls.W3_MOVIE_300,
            title = "أسرار تحضير القهوة المختصة والوصفات السريعة في المطبخ العصري",
            description = "خطوات بسيطة لنتيجة احترافية في كل صباح مع نصائح درجات الحرارة.",
            creatorName = "مذاق الشيف",
            creatorHandle = "@chef_taste",
            creatorAvatarId = "av_crown",
            duration = "08:33",
            viewsCount = 67400,
            likesCount = 5210,
            sharesCount = 490,
            orientation = VideoOrientation.HORIZONTAL,
            category = "طبخ",
            timeAgo = "منذ 4 أيام",
            visualStyle = MediaVisualStyle.COOKING_WARM
        ),
        VideoItem(
            id = "h7",
            mediaUriString = DemoMediaUrls.W3_BUNNY,
            title = "أسرار الإخراج السينمائي للأفلام الوثائقية واستخدام العدسات المقربة",
            description = "كيف تحكي قصة بصرية مشوقة مع زوايا إبداعية واختيار موسيقى تصويرية مؤثرة.",
            creatorName = "نور الإبداع",
            creatorHandle = "@noor_visuals",
            creatorAvatarId = "av_director",
            duration = "18:45",
            viewsCount = 92300,
            likesCount = 7650,
            sharesCount = 580,
            orientation = VideoOrientation.HORIZONTAL,
            category = "تقنية",
            timeAgo = "منذ 5 أيام",
            visualStyle = MediaVisualStyle.STUDIO_PHOTO
        ),
        VideoItem(
            id = "h8",
            mediaUriString = DemoMediaUrls.JELLYFISH_1MB,
            title = "مغامرة في جبال الألب السويسرية — تصوير سينمائي 4K بطائرات الدرون",
            description = "مشاهد جوية خلابة للقمم الثلجية والبحيرات الزرقاء في قلب الطبيعة البكر.",
            creatorName = "رحالة الصحراء",
            creatorHandle = "@desert_wanderer",
            creatorAvatarId = "av_traveler",
            duration = "12:20",
            viewsCount = 178000,
            likesCount = 14200,
            sharesCount = 1350,
            orientation = VideoOrientation.HORIZONTAL,
            category = "سفر",
            timeAgo = "منذ أسبوع",
            visualStyle = MediaVisualStyle.TRAVEL_SUNSET
        ),
        VideoItem(
            id = "h9",
            mediaUriString = DemoMediaUrls.SINTEL_480P,
            title = "بناء وتجهيز استوديو بودكاست احترافي بأقل تكلفة للمبتدئين",
            description = "تفاصيل الميكروفونات والكابلات وعوازل الصوت الصوتية لتسجيل محتوى بأعلى نقاء.",
            creatorName = "أحمد المنصور",
            creatorHandle = "@ahmed_studio",
            creatorAvatarId = "av_creator",
            duration = "15:10",
            viewsCount = 61200,
            likesCount = 4950,
            sharesCount = 380,
            orientation = VideoOrientation.HORIZONTAL,
            category = "تصميم",
            timeAgo = "منذ أسبوع",
            visualStyle = MediaVisualStyle.PODCAST_MIC,
            isMine = true
        ),
        VideoItem(
            id = "h10",
            // Highest-scoring widescreen clip: the single rare reels exception
            // uses the original 1080p source, fitted in full inside the feed.
            mediaUriString = DemoMediaUrls.SINTEL_1080P,
            title = "تحدي تجميع أقوى حاسوب مخصص للمونتاج والألعاب لعام 2026 مع بطاقة RTX 5090",
            description = "اختبار قوة المعالجة والتبريد المائي وسرعة الرندرة في برامج المونتاج السينمائي.",
            creatorName = "جيمر زون",
            creatorHandle = "@gamer_zone",
            creatorAvatarId = "av_gamer",
            duration = "24:18",
            viewsCount = 310000,
            likesCount = 22400,
            sharesCount = 2100,
            orientation = VideoOrientation.HORIZONTAL,
            category = "ألعاب",
            timeAgo = "منذ أسبوعين",
            visualStyle = MediaVisualStyle.GAMING_NEON
        )
    )

    // Verified browser-playable HTTPS MP4s. Small/range-capable files first so
    // the first ten clips cache quickly on mobile data; samplelib was removed
    // because it timed out during verification.
    val demoClipVideoUrls: List<String> = listOf(
        DemoMediaUrls.FLOWER,
        DemoMediaUrls.BBB_1MB,
        DemoMediaUrls.JELLYFISH_1MB,
        DemoMediaUrls.SINTEL_1MB,
        DemoMediaUrls.SAMPLE_640,
        DemoMediaUrls.W3_MOVIE_300,
        DemoMediaUrls.W3_BUNNY,
        DemoMediaUrls.SINTEL_480P,
        DemoMediaUrls.W3_SINTEL,
        DemoMediaUrls.BBB_2MB
    )

    val initialVerticalClips: List<VideoItem> = listOf(
        VideoItem(
            id = "v1",
            title = "جولة سريعة في شوارع السايبر بعد المطر 🌧️✨",
            description = "تصوير عمودي فائق الدقة لمدينة الأضواء وانعكاسات النيون #تصوير #نيون #clips",
            creatorName = "أحمد المنصور",
            creatorHandle = "@ahmed_studio",
            creatorAvatarId = "av_creator",
            duration = "00:38",
            viewsCount = 412000,
            likesCount = 38400,
            sharesCount = 2450,
            isSaved = true,
            orientation = VideoOrientation.VERTICAL,
            category = "سفر",
            timeAgo = "منذ ساعة",
            visualStyle = MediaVisualStyle.CYBER_CITY,
            mediaUriString = demoClipVideoUrls[0],
            isMine = true,
            musicTitle = "إيقاع ليلي هادئ • Neon Drift",
            comments = listOf(
                CommentItem("vc1", "مروان علي", "av_fire", "أجواء المطر مع الإضاءة خيالية 🔥", "منذ 20 دقيقة"),
                CommentItem("vc2", "رنا يوسف", "av_music", "ما نوع العدسة المستخدمة؟", "منذ 10 دقائق")
            )
        ),
        VideoItem(
            id = "v2",
            title = "خدعة انتقال مونتاج احترافية بالهاتف فقط 🎬",
            description = "جرّب هذا الانتقال في مقطعك القادم وستلاحظ الفرق فوراً #مونتاج #ابداع",
            creatorName = "نور الإبداع",
            creatorHandle = "@noor_visuals",
            creatorAvatarId = "av_director",
            duration = "00:24",
            viewsCount = 289000,
            likesCount = 24100,
            sharesCount = 1980,
            orientation = VideoOrientation.VERTICAL,
            category = "تقنية",
            timeAgo = "منذ 3 ساعات",
            visualStyle = MediaVisualStyle.TECH_WAVE,
            mediaUriString = demoClipVideoUrls[1],
            musicTitle = "صوت الانتقال الحركي • Studio Beat"
        ),
        VideoItem(
            id = "v3",
            title = "لقطة حماسية من بطولة الألعاب الإلكترونية 🎮⚡",
            description = "أسرع ردة فعل في الجولة النهائية! #ألعاب #تحدي",
            creatorName = "جيمر زون",
            creatorHandle = "@gamer_zone",
            creatorAvatarId = "av_bolt",
            duration = "00:45",
            viewsCount = 520000,
            likesCount = 49300,
            sharesCount = 3120,
            orientation = VideoOrientation.VERTICAL,
            category = "ألعاب",
            timeAgo = "منذ 6 ساعات",
            visualStyle = MediaVisualStyle.GAMING_NEON,
            mediaUriString = demoClipVideoUrls[2],
            musicTitle = "حماس البداية • Cyber Pulse"
        ),
        VideoItem(
            id = "v4",
            title = "غروب الشمس الذهبي فوق السحاب 🌅",
            description = "لحظات هدوء تستحق المشاهدة والتأمل #سفر #طبيعة",
            creatorName = "أحمد المنصور",
            creatorHandle = "@ahmed_studio",
            creatorAvatarId = "av_creator",
            duration = "00:31",
            viewsCount = 195000,
            likesCount = 17800,
            sharesCount = 870,
            orientation = VideoOrientation.VERTICAL,
            category = "سفر",
            timeAgo = "منذ يوم",
            visualStyle = MediaVisualStyle.TRAVEL_SUNSET,
            mediaUriString = demoClipVideoUrls[3],
            isMine = true,
            musicTitle = "نسيم الغروب • Acoustic Horizon"
        ),
        VideoItem(
            id = "v5",
            title = "تنسيق ألوان واجهة التطبيقات في 30 ثانية 🎨",
            description = "سر التناسق البصري في اختيار اللون الأساسي مع الخلفية الداكنة #تصميم",
            creatorName = "استوديو بيان",
            creatorHandle = "@bayan_design",
            creatorAvatarId = "av_ring",
            duration = "00:29",
            viewsCount = 143000,
            likesCount = 12450,
            sharesCount = 740,
            orientation = VideoOrientation.VERTICAL,
            category = "تصميم",
            timeAgo = "منذ يومين",
            visualStyle = MediaVisualStyle.DESIGN_GRID,
            mediaUriString = demoClipVideoUrls[4],
            musicTitle = "Lo-Fi التصميم • Chill Vibes"
        ),
        VideoItem(
            id = "v6",
            title = "أسرار الطهي السريع للوجبات الصحية في 5 دقائق 🍳",
            description = "وصفة سريعة ولذيذة لإفطار صباحي طاقوي ومتوازن #طبخ #صحي",
            creatorName = "مذاق الشيف",
            creatorHandle = "@chef_taste",
            creatorAvatarId = "av_crown",
            duration = "00:42",
            viewsCount = 210000,
            likesCount = 19400,
            sharesCount = 1120,
            orientation = VideoOrientation.VERTICAL,
            category = "طبخ",
            timeAgo = "منذ 3 أيام",
            visualStyle = MediaVisualStyle.COOKING_WARM,
            mediaUriString = demoClipVideoUrls[5],
            musicTitle = "مطبخ السعادة • Morning Kitchen"
        ),
        VideoItem(
            id = "v7",
            title = "أجمل مشاهد الطيران الشراعي فوق جبال البحر الأبيض المتوسط 🪂",
            description = "تجربة تحليق حر مذهلة وثوثيق بصري من منظور الصقر #مغامرة #سفر",
            creatorName = "رحالة الصحراء",
            creatorHandle = "@desert_wanderer",
            creatorAvatarId = "av_traveler",
            duration = "00:35",
            viewsCount = 380000,
            likesCount = 34200,
            sharesCount = 2100,
            orientation = VideoOrientation.VERTICAL,
            category = "سفر",
            timeAgo = "منذ 4 أيام",
            visualStyle = MediaVisualStyle.TRAVEL_SUNSET,
            mediaUriString = demoClipVideoUrls[6],
            musicTitle = "رياح الحرية • Sky High"
        ),
        VideoItem(
            id = "v8",
            title = "إعداد إضاءة الـ RGB الاحترافية لغرفة الألعاب والبث المباشر 💡",
            description = "توزيع الإضاءة الذكية لمظهر سينمائي مميز أثناء البث #تكنولوجيا #ألعاب",
            creatorName = "جيمر زون",
            creatorHandle = "@gamer_zone",
            creatorAvatarId = "av_gamer",
            duration = "00:40",
            viewsCount = 165000,
            likesCount = 15100,
            sharesCount = 940,
            orientation = VideoOrientation.VERTICAL,
            category = "ألعاب",
            timeAgo = "منذ 5 أيام",
            visualStyle = MediaVisualStyle.GAMING_NEON,
            mediaUriString = demoClipVideoUrls[7],
            musicTitle = "نيون جير • Cyber Gear"
        ),
        VideoItem(
            id = "v9",
            title = "كيف تصمم شعاراً احترافياً باستخدام الأشكال الهندسية البسيطة ✍️",
            description = "خطوات مبسطة لابتكار هوية بصرية خالدة لأي مشروع تجاري #تصميم",
            creatorName = "استوديو بيان",
            creatorHandle = "@bayan_design",
            creatorAvatarId = "av_badge",
            duration = "00:28",
            viewsCount = 112000,
            likesCount = 9800,
            sharesCount = 650,
            orientation = VideoOrientation.VERTICAL,
            category = "تصميم",
            timeAgo = "منذ أسبوع",
            visualStyle = MediaVisualStyle.DESIGN_GRID,
            mediaUriString = demoClipVideoUrls[8],
            musicTitle = "إلهام البصريات • Creator Flow"
        ),
        VideoItem(
            id = "v10",
            title = "كواليس مونتاج الأفلام الوثائقية وتجميع اللقطات الصامتة 🎞️",
            description = "كيف تدمج الصوت مع الحركة لإنتاج تأثير درامي مؤثر في قلوب المشاهدين #سينما",
            creatorName = "نور الإبداع",
            creatorHandle = "@noor_visuals",
            creatorAvatarId = "av_director",
            duration = "00:48",
            viewsCount = 295000,
            likesCount = 27600,
            sharesCount = 1840,
            orientation = VideoOrientation.VERTICAL,
            category = "تقنية",
            timeAgo = "منذ أسبوعين",
            visualStyle = MediaVisualStyle.PODCAST_MIC,
            mediaUriString = demoClipVideoUrls[9],
            musicTitle = "نبض السينما • Cinematic Pulse"
        )
    )

    val initialPosts: List<PostItem> = listOf(
        PostItem(
            id = "p1",
            authorName = "أحمد المنصور",
            authorHandle = "@ahmed_studio",
            authorAvatarId = "av_creator",
            content = "نزلنا للتو فيديو أفقي جديد عن تجهيز استوديو التصوير الاحترافي وإعدادات الإضاءة السينمائية! شاركونا رأيكم في التعليقات 🎥✨",
            timeAgo = "منذ ساعتين",
            visualStyle = MediaVisualStyle.STUDIO_PHOTO,
            mediaUriString = DemoMediaUrls.SINTEL_1080P,
            linkedVideoId = "h1",
            linkedVideoDuration = "14:28",
            likesCount = 1840,
            sharesCount = 215,
            isMine = true,
            comments = listOf(
                CommentItem("pc1", "يوسف طارق", "av_cool", "تمت المشاهدة، محتوى ممتاز كالعادة!", "منذ ساعة"),
                CommentItem("pc2", "مها خالد", "av_badge", "متحمسين للحلقة القادمة عن تعديل الألوان.", "منذ 40 دقيقة")
            )
        ),
        PostItem(
            id = "p2",
            authorName = "استوديو بيان",
            authorHandle = "@bayan_design",
            authorAvatarId = "av_badge",
            content = "أحياناً بساطة الخطوط العربية مع لون مميز واحد تصنع فارقاً ضخماً في راحة المستخدم داخل التطبيق. أي لون تفضلونه في وضع الحساب؟ 🎨",
            timeAgo = "منذ 4 ساعات",
            visualStyle = MediaVisualStyle.DESIGN_GRID,
            likesCount = 920,
            sharesCount = 84
        ),
        PostItem(
            id = "p3",
            authorName = "نور الإبداع",
            authorHandle = "@noor_visuals",
            authorAvatarId = "av_cyber",
            content = "سؤال اليوم لصناع المحتوى: هل تفضلون تصوير الفيديوهات الأفقية الطويلة أم المقاطع القصيرة السريعة؟ شاركونا تجربتكم 👇",
            timeAgo = "منذ 7 ساعات",
            likesCount = 650,
            sharesCount = 42,
            comments = listOf(
                CommentItem("pc3", "أحمد المنصور", "av_creator", "الاثنان يكملان بعضهما؛ الأفقي للشرح العميق والقصير للإلهام السريع.", "منذ 5 ساعات")
            )
        ),
        PostItem(
            id = "p4",
            authorName = "جيمر زون",
            authorHandle = "@gamer_zone",
            authorAvatarId = "av_gamer",
            content = "جربنا أقوى الأجهزة المحمولة على دقة عالية ومعدل إطارات ثابت — الفيديو العريض الكامل متاح الآن في الرئيسية وفي هذا المنشور 🎮🔥",
            timeAgo = "منذ يوم",
            visualStyle = MediaVisualStyle.GAMING_NEON,
            mediaUriString = DemoMediaUrls.BBB_1MB,
            linkedVideoId = "h4",
            linkedVideoDuration = "16:40",
            likesCount = 2310,
            sharesCount = 305
        )
    )

    val initialStories: List<UserStoryGroup> = listOf(
        UserStoryGroup(
            id = "st_noor",
            creatorName = "نور الإبداع",
            creatorHandle = "@noor_visuals",
            avatarPresetId = "av_cyber",
            slides = listOf(
                StorySlide(
                    id = "sl_noor_1",
                    mediaUriString = DemoMediaUrls.W3_BUNNY,
                    caption = "لقطة سريعة من كواليس التصوير الليلي بالعدسة الواسعة ✨🎥",
                    visualStyle = MediaVisualStyle.CYBER_CITY,
                    timeAgo = "منذ 25 دقيقة"
                ),
                StorySlide(
                    id = "sl_noor_2",
                    mediaUriString = DemoMediaUrls.SINTEL_1080P,
                    caption = "اختبار تدرج الألوان السينمائي قبل نشر الحلقة الكاملة",
                    visualStyle = MediaVisualStyle.STUDIO_PHOTO,
                    timeAgo = "منذ 15 دقيقة"
                )
            )
        ),
        UserStoryGroup(
            id = "st_bayan",
            creatorName = "استوديو بيان",
            creatorHandle = "@bayan_design",
            avatarPresetId = "av_badge",
            slides = listOf(
                StorySlide(
                    id = "sl_bayan_1",
                    mediaUriString = DemoMediaUrls.FLOWER,
                    caption = "تصميم شاشة رئيسية متدرجة الألوان — ما رأيكم في التناسق؟ 🎨",
                    visualStyle = MediaVisualStyle.DESIGN_GRID,
                    timeAgo = "منذ ساعة"
                )
            )
        ),
        UserStoryGroup(
            id = "st_gamer",
            creatorName = "جيمر زون",
            creatorHandle = "@gamer_zone",
            avatarPresetId = "av_fire",
            slides = listOf(
                StorySlide(
                    id = "sl_gamer_1",
                    mediaUriString = DemoMediaUrls.BBB_1MB,
                    caption = "أداء ثابت على 120 إطار في تجربة اليوم 🔥🎮",
                    visualStyle = MediaVisualStyle.GAMING_NEON,
                    timeAgo = "منذ ساعتين"
                ),
                StorySlide(
                    id = "sl_gamer_2",
                    mediaUriString = DemoMediaUrls.JELLYFISH_1MB,
                    caption = "تجهيز مكتب البث المباشر بإضاءة النيون الجديدة",
                    visualStyle = MediaVisualStyle.TECH_WAVE,
                    timeAgo = "منذ ساعة"
                )
            )
        ),
        UserStoryGroup(
            id = "st_chef",
            creatorName = "شيف تيست",
            creatorHandle = "@chef_taste",
            avatarPresetId = "av_crown",
            slides = listOf(
                StorySlide(
                    id = "sl_chef_1",
                    mediaUriString = DemoMediaUrls.SAMPLE_640,
                    caption = "فنجان إسبريسو بارد لبدء يوم مونتاج جديد ☕✨",
                    visualStyle = MediaVisualStyle.COOKING_WARM,
                    timeAgo = "منذ 3 ساعات"
                )
            )
        ),
        UserStoryGroup(
            id = "st_tariq",
            creatorName = "طارق رحال",
            creatorHandle = "@tariq_travel",
            avatarPresetId = "av_astro",
            isViewed = true,
            slides = listOf(
                StorySlide(
                    id = "sl_tariq_1",
                    mediaUriString = DemoMediaUrls.W3_BUNNY_MOVIE,
                    caption = "غروب مذهل من القمة — المقطع الكامل قريباً في clips 🌅",
                    visualStyle = MediaVisualStyle.TRAVEL_SUNSET,
                    timeAgo = "منذ 5 ساعات"
                )
            )
        )
    )

    /** Seed ideas so the suggestions board is useful from the first launch. */
    val initialSuggestions: List<FeatureSuggestion> = listOf(
        FeatureSuggestion(
            id = "sug_offline",
            title = "مشاهدة clips بدون إنترنت",
            description = "السماح بتنزيل المقاطع المفضلة لمشاهدتها لاحقاً في الطريق أو في الطائرة.",
            authorName = "نور",
            createdAt = 0L,
            likesCount = 34,
            comments = listOf(
                SuggestionComment("sugc_1", "بايان", "ميزة مهمة جداً، خصوصاً للسفر!", "منذ ساعتين")
            )
        ),
        FeatureSuggestion(
            id = "sug_playlists",
            title = "قوائم تشغيل مخصصة",
            description = "تجميع المقاطع والفيديوهات في قوائم باسماء اختيارية مع إمكانية ترتيبها.",
            authorName = "كريم",
            createdAt = 0L,
            likesCount = 21,
            comments = emptyList()
        ),
        FeatureSuggestion(
            id = "sug_slowmo",
            title = "تحكم أسرع في سرعة المشاهدة",
            description = "إضافة خيارات 0.5x و 2x داخل مشغل clips مثل مشغل الفيديو العريض.",
            authorName = "سارة",
            createdAt = 0L,
            likesCount = 12,
            comments = emptyList()
        )
    )
}
