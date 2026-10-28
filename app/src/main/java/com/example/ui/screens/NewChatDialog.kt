package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Contact
import com.example.ui.theme.*

@Composable
fun NewChatDialog(
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onSelectContact: (Contact) -> Unit,
    onCreateGroup: (String, List<String>) -> Unit
) {
    var isCreatingGroup by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    val selectedContactIds = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCreatingGroup) "Create Encrypted Group" else "New Encrypted Chat",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isCreatingGroup) {
                    // Option to Switch to Group Creation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .clickable { isCreatingGroup = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GroupAdd, null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("New Encrypted Group", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text("Sender Keys protocol for multi-user E2EE", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Contacts with Verified Identity Keys", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectContact(contact)
                                        onDismiss()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.displayName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(contact.displayName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                                        if (contact.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Verified, null, tint = StatusVerifiedGreen, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(contact.username, fontSize = 11.sp, color = SecondaryCyan)
                                }
                                Icon(Icons.Default.Lock, null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else {
                    // Group Creation Form
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Core Security Team") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Participants:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(contacts) { contact ->
                            val isSelected = selectedContactIds.contains(contact.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) selectedContactIds.remove(contact.id)
                                        else selectedContactIds.add(contact.id)
                                    }
                                    .background(if (isSelected) SurfaceVariantDark else Color.Transparent)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedContactIds.add(contact.id)
                                        else selectedContactIds.remove(contact.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryEmerald,
                                        checkmarkColor = OnPrimaryEmerald
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(contact.displayName, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (groupName.isNotBlank() && selectedContactIds.isNotEmpty()) {
                                onCreateGroup(groupName.trim(), selectedContactIds.toList())
                                onDismiss()
                            }
                        },
                        enabled = groupName.isNotBlank() && selectedContactIds.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = OnPrimaryEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Encrypted Group")
                    }
                }
            }
        }
    }
}
