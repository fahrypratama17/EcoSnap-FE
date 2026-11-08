package com.example.ecosnap.features.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape
import com.example.ecosnap.features.scan.components.ScanPointsCard
import com.example.ecosnap.features.scan.components.ScanTransactionCard
import com.example.ecosnap.features.scan.model.DepositTransaction

@Composable
fun ScanSuccessScreen(
  transaction: DepositTransaction,
  onSeeHistory: () -> Unit,
  onBackToHome: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        start = 24.dp,
        top = 46.dp,
        end = 24.dp,
        bottom = 24.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        Image(
          painter = painterResource(R.drawable.scan_success_illustration),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(width = 200.dp, height = 186.dp)
        )
      }

      item {
        Text(
          text = "Setor Berhasil!",
          style = MaterialTheme.typography.headlineMedium.copy(
            lineHeight = 30.sp,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green900,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      item {
        Text(
          text = "Sampahmu berhasil terverifikasi dan poin telah ditambahkan ke akunmu",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green900,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      item {
        ScanPointsCard(
          points = transaction.points,
          weightKg = transaction.weightKg
        )
      }

      item {
        ScanTransactionCard(transaction = transaction)
      }

      item {
        EcoButton(
          text = "Lihat Riwayat",
          onClick = onSeeHistory,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green700,
          contentColor = Color.White
        )
      }

      item {
        OutlinedButton(
          onClick = onBackToHome,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = EcoShape.large,
          border = BorderStroke(1.dp, EcoColor.Green900),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = EcoColor.Green900
          )
        ) {
          Text(
            text = "Kembali ke Beranda",
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }
}
