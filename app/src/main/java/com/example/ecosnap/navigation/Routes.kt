package com.example.ecosnap.navigation

import android.net.Uri

object Routes {
  const val SPLASH = "splash"
  const val ONBOARDING = "onboarding"

  const val AUTH_GRAPH = "auth_graph"
  const val LOGIN = "login"
  const val REGISTER = "register"
  const val FORGOT_PASSWORD = "forgot_password"
  const val CHECK_EMAIL = "check_email"
  const val NEW_PASSWORD = "new_password"
  const val HELP = "help"

  const val MAIN_GRAPH = "main_graph"

  const val HOME = "home"
  const val MAP = "map"
  const val SCAN = "scan"
  const val SCAN_RESULT = "scan_result"
  const val POINTS = "points"
  const val GAMES = "games"

  const val NOTIFICATION = "notification"

  const val PROFILE = "profile"
  const val PROFILE_ACCOUNT_SETTINGS = "profile_account_settings"
  const val PROFILE_HELP_CENTER = "profile_help_center"
  const val PROFILE_ACTIVITY_HISTORY = "profile_activity_history"
  const val PROFILE_REWARDS = "profile_rewards"
  const val PROFILE_ACHIEVEMENTS = "profile_achievements"
  const val PROFILE_ACTIVITY_REPORT = "profile_activity_report"
  const val PROFILE_PRIVACY_SECURITY = "profile_privacy_security"

  const val HISTORY = "history"
  const val MISSIONS = "missions"
  const val PARTNER_DETAIL = "partner_detail"

  const val GAMES_ALL = "games_all"
  const val GAME_DETAIL = "game_detail"
  const val GAME_PLAY = "game_play"
  const val GAME_LEADERBOARD = "game_leaderboard"
  const val DEPOSIT_LEADERBOARD = "deposit_leaderboard"

  fun scanResult(photoUri: Uri): String {
    val encodedUri = Uri.encode(
      photoUri.toString()
    )

    return "$SCAN_RESULT?photoUri=$encodedUri"
  }

  fun gameDetail(id: String): String = "$GAME_DETAIL/${android.net.Uri.encode(id)}"

  fun gamePlay(id: String): String = "$GAME_PLAY/${android.net.Uri.encode(id)}"

  fun partnerDetail(id: String): String {
    return "$PARTNER_DETAIL/${Uri.encode(id)}"
  }

  const val ARTICLES = "articles"
  const val ARTICLE_DETAIL = "article_detail"

  fun articleDetail(id: String): String {
    return "$ARTICLE_DETAIL/${Uri.encode(id)}"
  }

  const val VOUCHER_DETAIL = "voucher_detail"
  const val CALCULATOR = "calculator"

  fun voucherDetail(id: String): String {
    return "$VOUCHER_DETAIL/${Uri.encode(id)}"
  }
}