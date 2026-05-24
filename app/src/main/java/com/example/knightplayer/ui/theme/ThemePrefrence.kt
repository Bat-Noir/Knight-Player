package com.example.knightplayer.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemePreferences {

    private const val PREF_NAME = "knight_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_ACCENT_COLOR = "accent_color"
    private const val KEY_SEEKBAR_THICKNESS = "seekbar_thickness"
    private const val KEY_ICON_SCALE = "icon_scale"
    private const val KEY_TOP_ICON_ORDER = "top_icon_order"

    // 🔥 Playback Settings Keys
    private const val KEY_SEEK_ENABLED = "seek_enabled"
    private const val KEY_DOUBLE_TAP_DURATION = "double_tap_duration"
    private const val KEY_PINCH_ZOOM = "pinch_zoom"
    private const val KEY_FORCE_SOFTWARE = "force_software"

    private val _themeModeFlow = MutableStateFlow(ThemeModeOption.DARK)
    val themeModeFlow: StateFlow<ThemeModeOption> = _themeModeFlow.asStateFlow()

    private val _accentColorFlow = MutableStateFlow(AccentColorOption.RED)
    val accentColorFlow: StateFlow<AccentColorOption> = _accentColorFlow.asStateFlow()

    private val _seekbarThicknessFlow = MutableStateFlow(10f)
    val seekbarThicknessFlow: StateFlow<Float> = _seekbarThicknessFlow.asStateFlow()

    private val _iconScaleFlow = MutableStateFlow(1.0f)
    val iconScaleFlow: StateFlow<Float> = _iconScaleFlow.asStateFlow()

    // 🔥 Playback Settings State
    private val _seekEnabledFlow = MutableStateFlow(true)
    val seekEnabledFlow: StateFlow<Boolean> = _seekEnabledFlow.asStateFlow()

    private val _doubleTapDurationFlow = MutableStateFlow(15_000L) // Default 15s
    val doubleTapDurationFlow: StateFlow<Long> = _doubleTapDurationFlow.asStateFlow()

    private val _pinchToZoomFlow = MutableStateFlow(true)
    val pinchToZoomFlow: StateFlow<Boolean> = _pinchToZoomFlow.asStateFlow()

    private val _forceSoftwareDecoderFlow = MutableStateFlow(false)
    val forceSoftwareDecoderFlow: StateFlow<Boolean> = _forceSoftwareDecoderFlow.asStateFlow()

    // 🔥 Default Icon Order
    private val defaultTopIcons = listOf("SPEED", "AUDIO", "SUBTITLES", "PLAYLIST")
    private val _topIconOrderFlow = MutableStateFlow(defaultTopIcons)
    val topIconOrderFlow: StateFlow<List<String>> = _topIconOrderFlow.asStateFlow()

    private var isInitialized = false
    // Constants
    private const val KEY_HOLD_TO_FF_ENABLED = "hold_to_ff_enabled"
    private const val KEY_HOLD_FF_SPEED = "hold_ff_speed"

    // 🔥 AUDIO SETTINGS KEYS
    private const val KEY_VOLUME_BOOST = "volume_boost"
    private const val KEY_AUDIO_PASSTHROUGH = "audio_passthrough"

    // 🔥 AUDIO SETTINGS STATE
    private val _volumeBoostFlow = MutableStateFlow(false)
    val volumeBoostFlow: StateFlow<Boolean> = _volumeBoostFlow.asStateFlow()

    private val _audioPassthroughFlow = MutableStateFlow(true) // Default to hardware pass-through
    val audioPassthroughFlow: StateFlow<Boolean> = _audioPassthroughFlow.asStateFlow()

    // Flows
    private val _holdToFFEnabledFlow = MutableStateFlow(true)
    val holdToFFEnabledFlow: StateFlow<Boolean> = _holdToFFEnabledFlow.asStateFlow()

    private val _holdFFSpeedFlow = MutableStateFlow(2.0f) // Default 2x speed
    val holdFFSpeedFlow: StateFlow<Float> = _holdFFSpeedFlow.asStateFlow()

    // 🔥 SUBTITLE SETTINGS KEYS
    private const val KEY_SUB_FONT = "sub_font"
    private const val KEY_SUB_SIZE = "sub_size"
    private const val KEY_SUB_TEXT_COLOR = "sub_text_color"
    private const val KEY_SUB_BG_COLOR = "sub_bg_color"
    private const val KEY_SUB_BG_OPACITY = "sub_bg_opacity"
    private const val KEY_SUB_BORDER_COLOR = "sub_border_color"
    private const val KEY_SUB_BORDER_WIDTH = "sub_border_width"
    private const val KEY_SUB_POS_Y = "sub_pos_y"
    private const val KEY_SUB_POS_X = "sub_pos_x"

    // 🔥 SUBTITLE SETTINGS FLOWS (Fixed the DEFAULT to SYSTEM here)
    private val _subFontFlow = MutableStateFlow(SubtitleFont.SYSTEM)
    val subFontFlow: StateFlow<SubtitleFont> = _subFontFlow.asStateFlow()

    private val _subSizeFlow = MutableStateFlow(22f) // Default 22sp
    val subSizeFlow: StateFlow<Float> = _subSizeFlow.asStateFlow()

    private val _subTextColorFlow = MutableStateFlow(SubtitleColor.WHITE)
    val subTextColorFlow: StateFlow<SubtitleColor> = _subTextColorFlow.asStateFlow()

    private val _subBgColorFlow = MutableStateFlow(SubtitleColor.BLACK)
    val subBgColorFlow: StateFlow<SubtitleColor> = _subBgColorFlow.asStateFlow()

    private val _subBgOpacityFlow = MutableStateFlow(0.5f) // 50% opacity default
    val subBgOpacityFlow: StateFlow<Float> = _subBgOpacityFlow.asStateFlow()

    private val _subBorderColorFlow = MutableStateFlow(SubtitleColor.BLACK)
    val subBorderColorFlow: StateFlow<SubtitleColor> = _subBorderColorFlow.asStateFlow()

    private val _subBorderWidthFlow = MutableStateFlow(4f) // Default 4px stroke
    val subBorderWidthFlow: StateFlow<Float> = _subBorderWidthFlow.asStateFlow()

    private val _subPosYFlow = MutableStateFlow(40f) // Default bottom padding
    val subPosYFlow: StateFlow<Float> = _subPosYFlow.asStateFlow()

    private val _subPosXFlow = MutableStateFlow(0f) // Default center
    val subPosXFlow: StateFlow<Float> = _subPosXFlow.asStateFlow()

    fun init(context: Context) {
        if (!isInitialized) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            _themeModeFlow.value = ThemeModeOption.valueOf(prefs.getString(KEY_THEME_MODE, ThemeModeOption.DARK.name) ?: ThemeModeOption.DARK.name)
            _accentColorFlow.value = AccentColorOption.valueOf(prefs.getString(KEY_ACCENT_COLOR, AccentColorOption.RED.name) ?: AccentColorOption.RED.name)
            _seekbarThicknessFlow.value = prefs.getFloat(KEY_SEEKBAR_THICKNESS, 10f)
            _iconScaleFlow.value = prefs.getFloat(KEY_ICON_SCALE, 1.0f)

            // 🔥 THE FIX: Safely catch old saved data like "DEFAULT" so it doesn't crash
            val savedFontStr = prefs.getString(KEY_SUB_FONT, SubtitleFont.SYSTEM.name) ?: SubtitleFont.SYSTEM.name
            _subFontFlow.value = try {
                SubtitleFont.valueOf(savedFontStr)
            } catch (e: IllegalArgumentException) {
                SubtitleFont.SYSTEM // Fallback safely if it finds an old/deleted enum name
            }

            _subSizeFlow.value = prefs.getFloat(KEY_SUB_SIZE, 22f)
            _subTextColorFlow.value = SubtitleColor.valueOf(prefs.getString(KEY_SUB_TEXT_COLOR, SubtitleColor.WHITE.name) ?: SubtitleColor.WHITE.name)
            _subBgColorFlow.value = SubtitleColor.valueOf(prefs.getString(KEY_SUB_BG_COLOR, SubtitleColor.BLACK.name) ?: SubtitleColor.BLACK.name)
            _subBgOpacityFlow.value = prefs.getFloat(KEY_SUB_BG_OPACITY, 0.5f)
            _subBorderColorFlow.value = SubtitleColor.valueOf(prefs.getString(KEY_SUB_BORDER_COLOR, SubtitleColor.BLACK.name) ?: SubtitleColor.BLACK.name)
            _subBorderWidthFlow.value = prefs.getFloat(KEY_SUB_BORDER_WIDTH, 4f)
            _subPosYFlow.value = prefs.getFloat(KEY_SUB_POS_Y, 40f)
            _subPosXFlow.value = prefs.getFloat(KEY_SUB_POS_X, 0f)

            // 🔥 Initialize Playback Settings
            _seekEnabledFlow.value = prefs.getBoolean(KEY_SEEK_ENABLED, true)
            _doubleTapDurationFlow.value = prefs.getLong(KEY_DOUBLE_TAP_DURATION, 15_000L)
            _pinchToZoomFlow.value = prefs.getBoolean(KEY_PINCH_ZOOM, true)
            _forceSoftwareDecoderFlow.value = prefs.getBoolean(KEY_FORCE_SOFTWARE, false)

            _holdToFFEnabledFlow.value = prefs.getBoolean(KEY_HOLD_TO_FF_ENABLED, true)
            _holdFFSpeedFlow.value = prefs.getFloat(KEY_HOLD_FF_SPEED, 2.0f)

            val savedIcons = prefs.getString(KEY_TOP_ICON_ORDER, defaultTopIcons.joinToString(",")) ?: defaultTopIcons.joinToString(",")
            _topIconOrderFlow.value = savedIcons.split(",")

            _volumeBoostFlow.value = prefs.getBoolean(KEY_VOLUME_BOOST, false)
            _audioPassthroughFlow.value = prefs.getBoolean(KEY_AUDIO_PASSTHROUGH, true)
            isInitialized = true
        }
    }

    // 🔥 SETTERS
    fun setThemeMode(context: Context, mode: ThemeModeOption) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeFlow.value = mode
    }

    fun setAccentColor(context: Context, color: AccentColorOption) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_ACCENT_COLOR, color.name).apply()
        _accentColorFlow.value = color
    }

    fun setSeekbarThickness(context: Context, thickness: Float) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SEEKBAR_THICKNESS, thickness).apply()
        _seekbarThicknessFlow.value = thickness
    }

    fun setIconScale(context: Context, scale: Float) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_ICON_SCALE, scale).apply()
        _iconScaleFlow.value = scale
    }

    fun setTopIconOrder(context: Context, order: List<String>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TOP_ICON_ORDER, order.joinToString(",")).apply()
        _topIconOrderFlow.value = order
    }

    fun setSeekEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SEEK_ENABLED, enabled).apply()
        _seekEnabledFlow.value = enabled
    }

    fun setDoubleTapDuration(context: Context, ms: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(KEY_DOUBLE_TAP_DURATION, ms).apply()
        _doubleTapDurationFlow.value = ms
    }

    fun setPinchToZoom(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_PINCH_ZOOM, enabled).apply()
        _pinchToZoomFlow.value = enabled
    }

    fun setForceSoftwareDecoder(context: Context, force: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FORCE_SOFTWARE, force).apply()
        _forceSoftwareDecoderFlow.value = force
    }

    fun setVolumeBoost(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_VOLUME_BOOST, enabled).apply()
        _volumeBoostFlow.value = enabled
    }

    fun setAudioPassthrough(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUDIO_PASSTHROUGH, enabled).apply()
        _audioPassthroughFlow.value = enabled
    }

    fun setHoldToFFEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_HOLD_TO_FF_ENABLED, enabled).apply()
        _holdToFFEnabledFlow.value = enabled
    }

    fun setHoldFFSpeed(context: Context, speed: Float) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_HOLD_FF_SPEED, speed).apply()
        _holdFFSpeedFlow.value = speed
    }

    // 🔥 SUBTITLE SETTERS
    fun setSubFont(context: Context, font: SubtitleFont) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SUB_FONT, font.name).apply(); _subFontFlow.value = font }
    fun setSubSize(context: Context, size: Float) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SUB_SIZE, size).apply(); _subSizeFlow.value = size }
    fun setSubTextColor(context: Context, color: SubtitleColor) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SUB_TEXT_COLOR, color.name).apply(); _subTextColorFlow.value = color }
    fun setSubBgColor(context: Context, color: SubtitleColor) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SUB_BG_COLOR, color.name).apply(); _subBgColorFlow.value = color }
    fun setSubBgOpacity(context: Context, opacity: Float) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SUB_BG_OPACITY, opacity).apply(); _subBgOpacityFlow.value = opacity }
    fun setSubBorderColor(context: Context, color: SubtitleColor) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SUB_BORDER_COLOR, color.name).apply(); _subBorderColorFlow.value = color }
    fun setSubBorderWidth(context: Context, width: Float) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SUB_BORDER_WIDTH, width).apply(); _subBorderWidthFlow.value = width }
    fun setSubPosY(context: Context, y: Float) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SUB_POS_Y, y).apply(); _subPosYFlow.value = y }
    fun setSubPosX(context: Context, x: Float) { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_SUB_POS_X, x).apply(); _subPosXFlow.value = x }
}
