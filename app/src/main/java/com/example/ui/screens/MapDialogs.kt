package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.HouseSummary
import com.example.data.Household
import com.example.ui.theme.EmeraldPrimary

@Composable
fun HouseholdPickerDialog(
    households: List<Household>,
    unmappedHouseholdIds: Set<Long>,
    onDismiss: () -> Unit,
    onSelect: (Household) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(if (unmappedHouseholdIds.isNotEmpty()) 0 else 1) }

    val filteredList = remember(households, search, selectedTab) {
        val byTab = when (selectedTab) {
            0 -> households.filter { unmappedHouseholdIds.contains(it.id) }
            else -> households
        }
        if (search.isBlank()) byTab
        else byTab.filter { it.houseNo.contains(search.trim(), ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("เลือกครัวเรือนที่จะปักหมุด", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "ปิด")
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("ยังไม่มีพิกัด (${unmappedHouseholdIds.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("ทั้งหมด (${households.size})") }
                    )
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("ค้นหาบ้านเลขที่...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("ไม่พบรายการครัวเรือน", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { hh ->
                            val hasLoc = hh.latitude != null && hh.longitude != null
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(hh) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (hasLoc) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.08f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("บ้านเลขที่ ${hh.houseNo}", fontWeight = FontWeight.Bold)
                                        val villageInfo = if (hh.villageNo.isNotBlank()) "หมู่ ${hh.villageNo} " else ""
                                        val subdistrictInfo = if (hh.subdistrict.isNotBlank()) "ต.${hh.subdistrict}" else ""
                                        if (villageInfo.isNotBlank() || subdistrictInfo.isNotBlank()) {
                                            Text("$villageInfo$subdistrictInfo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (hasLoc) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Text("มีพิกัดเดิม", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                    } else {
                                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary.copy(alpha = 0.2f)) {
                                            Text("ยังไม่มีพิกัด", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnpinnedHousesDialog(
    unmappedHouses: List<HouseSummary>,
    onDismiss: () -> Unit,
    onPinHouse: (HouseSummary) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(unmappedHouses, search) {
        if (search.isBlank()) unmappedHouses
        else unmappedHouses.filter { it.houseNo.contains(search.trim(), ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("บ้านที่ยังไม่ได้ระบุพิกัด", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("เลือกบ้านเพื่อนำหมุดไปวางบนแผนที่", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "ปิด")
                    }
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("ค้นหาบ้านเลขที่...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("ทุกครัวเรือนได้รับการปักหมุดครบถ้วนแล้ว 🎉", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { house ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPinHouse(house) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("บ้านเลขที่ ${house.houseNo}", fontWeight = FontWeight.Bold)
                                        Text("สมาชิก ${house.totalMembers} คน (สูงอายุ ${house.elderly}, เด็ก ${house.earlyChild + house.schoolAge})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { onPinHouse(house) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Icon(Icons.Filled.PinDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ปักหมุด", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
