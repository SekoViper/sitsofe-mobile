package com.sitsofe.scanner.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sitsofe.scanner.ui.PreviewMocks

enum class LogoutAction { LOGOUT, SWITCH }

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onActionDone: (LogoutAction) -> Unit
) {
    val profile = vm.profile()

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showSwitchConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            // --- Profile Info ---
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profile", style = MaterialTheme.typography.titleMedium)
                    ProfileRow("Name", profile.name ?: "—")
                    ProfileRow("Phone", profile.phone ?: "—")
                    ProfileRow("Company", profile.company ?: "—")
                    ProfileRow("Role", profile.role ?: "—")
                    ProfileRow("Currency", profile.currency ?: "—")
                    ProfileRow("Email", profile.email ?: "—")
                }
            }

            // --- Actions ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showLogoutConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log out") }

                OutlinedButton(onClick = { showSwitchConfirm = true }) {
                    Text("Switch account")
                }
            }
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = { Text("Confirm logout") },
                text = { Text("You will be signed out of this device and local data will be cleared.") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutConfirm = false
                        vm.logout()
                        onActionDone(LogoutAction.LOGOUT)
                    }) { Text("Log out", fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
            )
        }

        if (showSwitchConfirm) {
            AlertDialog(
                onDismissRequest = { showSwitchConfirm = false },
                title = { Text("Switch account") },
                text = { Text("You’ll be signed out. The last used email will be kept to help you sign in faster.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSwitchConfirm = false
                        vm.switchAccount(preserveEmail = null)
                        onActionDone(LogoutAction.SWITCH)
                    }) { Text("Continue", fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showSwitchConfirm = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun SettingsScreenPreview() {
    val context = LocalContext.current
    SettingsScreen(
        vm = SettingsViewModel(
            PreviewMocks.MockSessionPrefs(context),
            PreviewMocks.MockAppDb(context)
        ),
        onActionDone = { }
    )
}
