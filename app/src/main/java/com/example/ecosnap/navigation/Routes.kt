package com.example.ecosnap.navigation

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
  const val POINTS = "points"
  const val GAMES = "games"

  const val NOTIFICATION = "notification"
  const val PROFILE = "profile"
  const val HISTORY = "history"
  const val MISSIONS = "missions"
  const val PARTNER_DETAIL = "partner_detail"

  fun partnerDetail(id: String) = "$PARTNER_DETAIL/$id"
}