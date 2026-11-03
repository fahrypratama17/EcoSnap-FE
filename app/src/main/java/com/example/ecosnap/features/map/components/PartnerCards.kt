package com.example.ecosnap.features.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.map.model.WasteMaterial
import com.example.ecosnap.features.map.model.WastePartner

@Composable
fun PartnerMaterialChip(
  material: WasteMaterial,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = EcoColor.Green100
  ) {
    Text(
      text = material.label,
      color = EcoColor.Green800,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(
        horizontal = 9.dp,
        vertical = 5.dp
      )
    )
  }
}

@Composable
fun PartnerSummaryCard(
  partner: WastePartner,
  isFavorite: Boolean,
  onFavoriteClick: () -> Unit,
  onDetailClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(12.dp),
    shadowElevation = 5.dp
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "MITRA",
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.Green700
      )

      Spacer(Modifier.weight(1f))

      IconButton(
        onClick = onFavoriteClick,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Lucide.Heart,
          contentDescription = if (isFavorite) {
            "Hapus favorit"
          } else {
            "Tambahkan favorit"
                 },
          tint = if (isFavorite) {
            EcoColor.TextError
          } else {
            EcoColor.Green800
          }
        )
      }
    }

    Spacer(Modifier.height(3.dp))

    Text(
      text = partner.name,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp
      ),
      color = EcoColor.Green900
    )

    Text(
      text = partner.area,
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(7.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      partner.acceptedMaterials.take(3).forEach {
        PartnerMaterialChip(material = it)
      }
    }

    Spacer(Modifier.height(12.dp))

    EcoButton(
      text = "Lihat detail mitra",
      onClick = onDetailClick,
      containerColor = EcoColor.Green700,
      contentColor = Color.White,
      endIcon = {
        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }
    )
  }
}

@Composable
fun PartnerAcceptedMaterialCard(
  materials: Set<WasteMaterial>,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Recycle,
        contentDescription = null,
        tint = EcoColor.Green800,
        modifier = Modifier.size(20.dp)
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = "Jenis Sampah Diterima",
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(10.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      materials.forEach {
        PartnerMaterialChip(it)
      }
    }

    Spacer(Modifier.height(10.dp))

    Text(
      text = "Pastikan kondisi dna jenis material sesuai dengan ketentuan mitra sebelum menyetor.",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )
  }
}

@Composable
fun PartnerInfoRow(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .background(
          EcoColor.Green100,
          RoundedCornerShape(12.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(Modifier.height(12.dp))

    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(3.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )
    }
  }
}

@Composable
fun PartnerInformationCard(
  partner: WastePartner,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(
      text = "Informasi Lokasi",
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(12.dp))

    PartnerInfoRow(
      icon = Lucide.MapPin,
      title = "Alamat Mitra",
      description = partner.address
    )

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 12.dp),
      color = EcoColor.Green200
    )

    PartnerInfoRow(
      icon = Lucide.Clock3,
      title = "Jam Operasional",
      description = partner.openingHours
    )

    HorizontalDivider(
      modifier = modifier.padding(vertical = 12.dp),
      color = EcoColor.Green200
    )

    PartnerInfoRow(
      icon = Lucide.Phone,
      title = "Kontak Mitra",
      description = partner.phone
    )
  }
}

@Composable
private fun DepositStep(
  number: Int,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(
          EcoColor.Green100,
          CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.Green800
      )
    }

    Spacer(Modifier.width(10.dp))

    Column{
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(3.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )
    }
  }
}

@Composable
fun PartnerDepositsStepsCard(
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(
      text = "Cara Menyetorkan",
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(16.dp))

    DepositStep(
      number = 1,
      title = "Pilah sampah",
      description = "Pisahkan sampah berdasarkan jenis material."
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 2,
      title = "Datang ke mitra",
      description = "Bawa material ke lokasi mitra pada jam layanan"
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 3,
      title = "Timbang sampah",
      description = "Mitra memeriksa dan memasukkan berat hasil penimbangan"
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 4,
      title = "Scan QR transaksi",
      description = "Pindai QR dari mitra menggunakan EcoSnap untuk validasi transaksi."
    )
  }
}