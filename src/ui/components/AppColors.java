package ui.components;

import java.awt.*;

/**
 * Bright modern SaaS enterprise colour palette.
 * Sidebar: Deep Indigo · Content: Clean White/Light Gray · Accent: Indigo/Violet
 */
public class AppColors {

    // ── Background layers ────────────────────────────────────────────────────
    public static final Color BG_BASE      = new Color(0xF0F2F8);   // page bg
    public static final Color BG_SECONDARY = new Color(0xE8EBF3);   // subtle bg
    public static final Color BG_CARD      = new Color(0xFFFFFF);   // white card
    public static final Color BG_SIDEBAR   = new Color(0x1E1B4B);   // deep indigo
    public static final Color BG_SIDEBAR2  = new Color(0x2D2A5E);   // sidebar secondary
    public static final Color BG_FIELD     = new Color(0xF5F6FA);   // input background
    public static final Color BG_HOVER     = new Color(0xEEF0F9);   // hover state

    // ── Accent colours ───────────────────────────────────────────────────────
    public static final Color ACCENT       = new Color(0x6366F1);   // indigo
    public static final Color ACCENT_DARK  = new Color(0x4F46E5);   // indigo darker
    public static final Color ACCENT_LIGHT = new Color(0xEEF2FF);   // indigo tint
    public static final Color VIOLET       = new Color(0x8B5CF6);   // violet
    public static final Color SUCCESS      = new Color(0x10B981);   // emerald
    public static final Color SUCCESS_BG   = new Color(0xD1FAE5);   // emerald light
    public static final Color ERROR        = new Color(0xEF4444);   // red
    public static final Color ERROR_BG     = new Color(0xFEE2E2);   // red light
    public static final Color WARNING      = new Color(0xF59E0B);   // amber

    // Legacy alias so existing code that references NEON_CYAN still compiles
    public static final Color NEON_CYAN    = ACCENT;

    // ── Text colours ─────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0x111827); // near-black
    public static final Color TEXT_SECONDARY = new Color(0x6B7280); // cool gray
    public static final Color TEXT_MUTED     = new Color(0x9CA3AF); // muted
    public static final Color TEXT_SIDEBAR   = new Color(0xC7D2FE); // indigo-200
    public static final Color TEXT_SIDEBAR_ACTIVE = new Color(0xFFFFFF);

    // ── Borders / Dividers ───────────────────────────────────────────────────
    public static final Color BORDER        = new Color(0xE5E7EB);  // gray-200
    public static final Color BORDER_FOCUS  = new Color(0x6366F1);  // indigo focus
    public static final Color DIVIDER       = new Color(0x374151);  // sidebar divider
    public static final Color BORDER_GLASS  = new Color(229, 231, 235, 120); // legacy alias
}
