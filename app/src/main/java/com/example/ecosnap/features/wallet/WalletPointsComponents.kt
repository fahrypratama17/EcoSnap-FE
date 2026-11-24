package com.example.ecosnap.features.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Calculator
import com.composables.icons.lucide.Coins
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sprout
import com.composables.icons.lucide.Ticket
import com.composables.icons.lucide.QrCode
import com.example.ecosnap.R
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.History
import com.example.ecosnap.core.designsystem.theme.EcoColor

internal val WalletDark = Color(0xFF254031)
internal val WalletMid = Color(0xFF3E6852)
internal val WalletTile = Color(0xFF56836B)

/** Points still missing for [cost]; null when the balance is unknown or already sufficient. */
internal fun pointsShortfall(wallet: Wallet?, cost: Long): Long? =
  wallet?.let { (cost - it.balance).takeIf { delta -> delta > 0 } }

@Composable
internal fun PointsHeading(modifier: Modifier = Modifier) {
  Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      "Tukar Poin",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
      ),
      color = EcoColor.Green900
    )
    Spacer(Modifier.height(2.dp))
    Text(
      "Tukarkan EcoPoints dengan voucher simulasi.",
      fontSize = 11.sp,
      lineHeight = 15.sp,
      color = EcoColor.Green700
    )
  }
}

@Composable
internal fun PointsBalanceHero(state: PointsUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = WalletDark,
    shadowElevation = 4.dp
  ) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Color(0xFF537D66), modifier = Modifier.size(52.dp)) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Lucide.Sprout, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
          }
        }
        Spacer(Modifier.width(14.dp))
        Column {
          Text("Poinmu saat ini", fontSize = 11.sp, lineHeight = 14.sp, color = Color.White.copy(alpha = 0.9f))
          Spacer(Modifier.height(2.dp))
          val wallet = state.wallet
          when {
            wallet != null -> Row(verticalAlignment = Alignment.Bottom) {
              Text(
                formatNumber(wallet.balance),
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(Modifier.width(5.dp))
              Text("poin", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(bottom = 3.dp))
            }
            state.walletError != null -> {
              Text(state.walletError, fontSize = 13.sp, color = Color.White)
              TextButton(onClick = onRetry) { Text("Coba lagi", color = Color.White) }
            }
            else -> Text("Memuat…", fontSize = 16.sp, color = Color.White)
          }
        }
      }
      state.wallet?.let { wallet ->
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.45f))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Lucide.Leaf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(6.dp))
          Text(
            "Didapat ${formatNumber(wallet.totalEarned)} • Terpakai ${formatNumber(wallet.totalSpent)}",
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
          )
        }
        if (state.walletError != null) {
          Spacer(Modifier.height(4.dp))
          Text("Saldo mungkin belum terbaru: ${state.walletError}", fontSize = 11.sp, color = Color.White)
        }
      }
    }
  }
}

/** Truthful replacement for the original promo: describes the real, simulated voucher flow only. */
@Composable
internal fun PointsSimulationBanner(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFFA4D2B5),
    shadowElevation = 4.dp
  ) {
    Row(
      Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          "Tukar poin,\ncoba alur voucher!",
          fontSize = 15.sp,
          lineHeight = 20.sp,
          fontWeight = FontWeight.Bold,
          color = EcoColor.Green900
        )
        Spacer(Modifier.height(6.dp))
        Text(
          "Voucher simulasi untuk demo EcoSnap, berlaku 7 hari sejak penukaran dan tidak berlaku di merchant nyata.",
          fontSize = 10.sp,
          lineHeight = 14.sp,
          color = WalletDark
        )
      }
      Spacer(Modifier.width(8.dp))
      Image(
        painter = painterResource(R.drawable.points_promo_illustration),
        contentDescription = "Ilustrasi penukaran poin EcoSnap",
        modifier = Modifier.size(width = 110.dp, height = 92.dp),
        contentScale = ContentScale.Fit
      )
    }
  }
}

internal class WalletQuickAction(val title: String, val icon: ImageVector, val onClick: () -> Unit)

/** Original two primary tiles (Scan QR, Kalkulator) plus history/vouchers/XP as secondary links. */
@Composable
internal fun PointsQuickActions(
  primary: List<WalletQuickAction>,
  secondary: List<WalletQuickAction>,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = WalletDark,
    shadowElevation = 4.dp
  ) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
      Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        primary.forEach { action ->
          Column(
            Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = action.onClick)
              .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              Modifier.size(58.dp).background(WalletTile, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(action.icon, contentDescription = action.title, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(action.title, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.45f))
      Spacer(Modifier.height(6.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        secondary.forEach { action ->
          Row(
            Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = action.onClick)
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(action.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(action.title, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
          }
        }
      }
    }
  }
}

internal fun walletPrimaryActions(onScanQr: () -> Unit, onEstimate: () -> Unit) = listOf(
  WalletQuickAction("Scan QR", Lucide.QrCode, onScanQr),
  WalletQuickAction("Kalkulator", Lucide.Calculator, onEstimate)
)

internal fun walletSecondaryActions(onHistory: () -> Unit, onVouchers: () -> Unit, onLeaderboard: () -> Unit) = listOf(
  WalletQuickAction("Riwayat", Lucide.History, onHistory),
  WalletQuickAction("Voucher", Lucide.Ticket, onVouchers),
  WalletQuickAction("XP", Lucide.Trophy, onLeaderboard)
)

@Composable
internal fun PointsSectionTitle(title: String, count: Int, modifier: Modifier = Modifier) {
  Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    Text(title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    Text("$count reward", fontSize = 11.sp, color = EcoColor.Green700)
  }
}

@Composable
internal fun RewardGridCard(
  reward: Reward,
  wallet: Wallet?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val soldOut = !reward.available || reward.stock <= 0
  val shortfall = pointsShortfall(wallet, reward.costPoints)
  val canRedeem = !soldOut && shortfall == null
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(1.dp, Color(0xFFD5DCD8)),
    shadowElevation = 3.dp
  ) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
      Box(
        Modifier
          .fillMaxWidth()
          .height(64.dp)
          .background(if (soldOut) Color(0xFF9AA8A0) else Color(0xFF4E4EEE), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Lucide.HandCoins, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
      }
      Spacer(Modifier.height(8.dp))
      Text(reward.name, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green900, maxLines = 2)
      if (reward.description.isNotBlank()) {
        Spacer(Modifier.height(2.dp))
        Text(reward.description, fontSize = 10.sp, lineHeight = 14.sp, color = EcoColor.Green900, maxLines = 2)
      }
      Spacer(Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Lucide.Leaf, contentDescription = null, tint = EcoColor.Green700, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text("${formatNumber(reward.costPoints)} poin", fontSize = 10.sp, lineHeight = 14.sp, color = EcoColor.Green900)
      }
      Spacer(Modifier.height(2.dp))
      Text(
        if (soldOut) "Stok habis" else "Stok ${formatNumber(reward.stock)}",
        fontSize = 9.sp,
        lineHeight = 12.sp,
        color = if (soldOut) EcoColor.TextError else EcoColor.Green700
      )
      Spacer(Modifier.height(6.dp))
      Box(
        Modifier
          .fillMaxWidth()
          .height(23.dp)
          .clip(RoundedCornerShape(50))
          .background(if (canRedeem) WalletMid else Color(0xFFC4D1CA))
          .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
      ) {
        Text(
          "Lihat & Tukar",
          fontSize = 10.sp,
          lineHeight = 13.sp,
          fontWeight = FontWeight.Medium,
          color = if (canRedeem) Color.White else Color(0xFF6A7A72)
        )
      }
      Box(Modifier.fillMaxWidth().height(18.dp).padding(top = 4.dp), contentAlignment = Alignment.CenterStart) {
        if (shortfall != null && !soldOut) {
          Text("Kurang ${formatNumber(shortfall)} poin", fontSize = 9.sp, lineHeight = 12.sp, color = EcoColor.Green900)
        }
      }
    }
  }
}

@Composable
internal fun WalletBackLink(label: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier.clickable(onClick = onBack).padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(Lucide.ArrowLeft, contentDescription = null, tint = EcoColor.Green700, modifier = Modifier.size(14.dp))
    Spacer(Modifier.width(6.dp))
    Text(label, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, color = EcoColor.Green700)
  }
}

@Composable
internal fun WalletHeroTile(icon: ImageVector, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth().height(164.dp),
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF87C5A0),
    shadowElevation = 3.dp
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(72.dp))
    }
  }
}

@Composable
internal fun WalletOutlinedCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 2.dp
  ) {
    Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
  }
}

@Composable
internal fun WalletPillButton(
  text: String,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth().height(44.dp),
    shape = RoundedCornerShape(50),
    color = if (enabled) WalletMid else Color(0xFFC4D1CA),
    shadowElevation = if (enabled) 3.dp else 0.dp
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (enabled) Color.White else Color(0xFF6A7A72)
      )
    }
  }
}

@Composable
internal fun WalletCostLine(cost: Long, modifier: Modifier = Modifier) {
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(Lucide.Coins, contentDescription = null, tint = EcoColor.Green800, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(6.dp))
    Text("${formatNumber(cost)} poin", fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, color = EcoColor.Green800)
  }
}
