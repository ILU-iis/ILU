// ==========================================================================
// ILU DASHBOARD APPLICATION SCRIPT
// Firebase Firestore Integration (Compat Version for local file:// support)
// Welcome Page, Role Selection, Login Auth & Developer Account Management
// ==========================================================================

// Your web app's Firebase configuration
var firebaseConfig = {
  apiKey: "AIzaSyB9c9PGIIn6YNa3jNkxgLv98GqqlhHV5uE",
  authDomain: "ilu-app-2d650.firebaseapp.com",
  projectId: "ilu-app-2d650",
  storageBucket: "ilu-app-2d650.firebasestorage.app",
  messagingSenderId: "548843396145",
  appId: "1:548843396145:web:bec425c2e0072ee542c910",
  measurementId: "G-8Q8T7ZF2ZZ"
};

// Initialize Firebase App & Firestore
firebase.initializeApp(firebaseConfig);
var db = firebase.firestore();

// Global State
var selectedLoginRole = 'hospital'; // 'hospital' or 'developer'
var loggedInAccount = null; // { id, username, password, role }
var allAccounts = [];
var allChats = [];
var allGratitudes = [];
var allCrisisLogs = [];
var allPeriods = [];
var usersMap = new Map();
var activeUser = null;
var dailyChartInstance = null;

// Seed Accounts Default
var DEFAULT_ACCOUNTS = [
  { username: "RSUD Sayyidiman Magetan", password: "magetanngangeni", role: "hospital" },
  { username: "I Listen To You", password: "wearethewinner", role: "developer" }
];

// Initialize on page load
window.onload = function() {
  initSeedAccounts();
  initAccountsListener();
};

// ==========================================================================
// 1. PAGE NAVIGATION & FLOW
// ==========================================================================

function hideAllSections() {
  document.getElementById('welcomeView').classList.add('hidden');
  document.getElementById('roleSelectView').classList.add('hidden');
  document.getElementById('loginView').classList.add('hidden');
  document.getElementById('appContainer').classList.add('hidden');
}

function navToWelcome() {
  hideAllSections();
  document.getElementById('welcomeView').classList.remove('hidden');
}

function navToRoleSelect() {
  hideAllSections();
  document.getElementById('roleSelectView').classList.remove('hidden');
}

function navToLogin(role) {
  selectedLoginRole = role;
  hideAllSections();
  
  var loginTitle = document.getElementById('loginTitle');
  var loginSubtitle = document.getElementById('loginSubtitle');
  var errorAlert = document.getElementById('loginErrorAlert');

  errorAlert.classList.add('hidden');
  document.getElementById('inputUsername').value = '';
  document.getElementById('inputPassword').value = '';

  if (role === 'developer') {
    loginTitle.innerText = 'Login Developer';
    loginSubtitle.innerText = 'Masukkan Username & Password Developer';
  } else {
    loginTitle.innerText = 'Login Rumah Sakit';
    loginSubtitle.innerText = 'Masukkan Username & Password Rumah Sakit';
  }

  document.getElementById('loginView').classList.remove('hidden');
}

function handleLogout() {
  loggedInAccount = null;
  navToWelcome();
}

// ==========================================================================
// 2. FIRESTORE ACCOUNTS SEEDING & AUTHENTICATION
// ==========================================================================

function initSeedAccounts() {
  db.collection("accounts").get().then(function(snapshot) {
    if (snapshot.empty) {
      console.log("Koleksi accounts kosong. Menginisialisasi akun bawaan...");
      DEFAULT_ACCOUNTS.forEach(function(acc) {
        db.collection("accounts").add(acc);
      });
    }
  }).catch(function(err) {
    console.error("Gagal mengecek seed accounts:", err);
  });
}

function initAccountsListener() {
  db.collection("accounts").onSnapshot(function(snapshot) {
    allAccounts = [];
    snapshot.forEach(function(doc) {
      allAccounts.push(Object.assign({ id: doc.id }, doc.data()));
    });

    if (loggedInAccount && loggedInAccount.role === 'developer') {
      renderAccountsTable();
    }
  });
}

function handleLoginSubmit(event) {
  event.preventDefault();

  var uNameInput = document.getElementById('inputUsername').value.trim();
  var pWordInput = document.getElementById('inputPassword').value.trim();
  var errorAlert = document.getElementById('loginErrorAlert');

  // Match against Firestore accounts for the selected role
  var match = allAccounts.find(function(acc) {
    return acc.role === selectedLoginRole && 
           acc.username.toLowerCase() === uNameInput.toLowerCase() && 
           acc.password === pWordInput;
  });

  // Also check fallback if Firestore collection is still syncing
  if (!match) {
    match = DEFAULT_ACCOUNTS.find(function(acc) {
      return acc.role === selectedLoginRole && 
             acc.username.toLowerCase() === uNameInput.toLowerCase() && 
             acc.password === pWordInput;
    });
  }

  if (match) {
    loggedInAccount = match;
    errorAlert.classList.add('hidden');
    openDashboard();
  } else {
    errorAlert.classList.remove('hidden');
  }
}

function openDashboard() {
  hideAllSections();
  document.getElementById('appContainer').classList.remove('hidden');

  updateRoleUI();
  initDataListeners();
}

function updateRoleUI() {
  var roleBadge = document.getElementById('roleBadge');
  var roleBadgeIcon = document.getElementById('roleBadgeIcon');
  var roleBadgeText = document.getElementById('roleBadgeText');
  var devStatsPanel = document.getElementById('devStatsPanel');
  var accountManagementPanel = document.getElementById('accountManagementPanel');
  var usernameDisplay = document.getElementById('currentUsernameText');

  usernameDisplay.innerText = loggedInAccount ? loggedInAccount.username : 'User';

  if (loggedInAccount && loggedInAccount.role === 'developer') {
    roleBadge.className = 'badge-role developer';
    roleBadgeIcon.className = 'fa-solid fa-code';
    roleBadgeText.innerText = 'Developer';
    devStatsPanel.classList.remove('hidden');
    accountManagementPanel.classList.remove('hidden');
    renderAccountsTable();
  } else {
    roleBadge.className = 'badge-role hospital';
    roleBadgeIcon.className = 'fa-solid fa-hospital-user';
    roleBadgeText.innerText = 'Rumah Sakit';
    devStatsPanel.classList.add('hidden');
    accountManagementPanel.classList.add('hidden');
  }
}

// ==========================================================================
// 3. DEVELOPER ACCOUNT MANAGEMENT (CRUD)
// ==========================================================================

function renderAccountsTable() {
  var tbody = document.getElementById('accountsTbody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (allAccounts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#94A3B8;">Belum ada akun terdaftar.</td></tr>';
    return;
  }

  allAccounts.forEach(function(acc) {
    var tr = document.createElement('tr');
    var isSelf = loggedInAccount && loggedInAccount.id === acc.id;

    var roleLabel = acc.role === 'developer' 
      ? '<span style="color:#B45309; font-weight:600;"><i class="fa-solid fa-code"></i> Developer</span>'
      : '<span style="color:#0284C7; font-weight:600;"><i class="fa-solid fa-hospital"></i> Rumah Sakit</span>';

    var deleteBtn = isSelf 
      ? '<span style="font-size:11px; color:#94A3B8;">(Sedang Digunakan)</span>'
      : '<button class="btn-delete-sm" onclick="deleteAccount(\'' + acc.id + '\', \'' + acc.username + '\')"><i class="fa-solid fa-trash"></i> Hapus</button>';

    tr.innerHTML = 
      '<td><strong>' + acc.username + '</strong></td>' +
      '<td><code>' + acc.password + '</code></td>' +
      '<td>' + roleLabel + '</td>' +
      '<td>' +
        '<button class="btn-edit-sm" onclick="openEditAccountModal(\'' + acc.id + '\')"><i class="fa-solid fa-pen"></i> Edit</button>' +
        deleteBtn +
      '</td>';
    tbody.appendChild(tr);
  });
}

function openAddAccountModal() {
  document.getElementById('accountModalTitle').innerText = 'Tambah Akun Baru';
  document.getElementById('editAccountId').value = '';
  document.getElementById('accUsername').value = '';
  document.getElementById('accPassword').value = '';
  document.getElementById('accRole').value = 'hospital';
  document.getElementById('accountModal').classList.remove('hidden');
}

function openEditAccountModal(docId) {
  var acc = allAccounts.find(function(a) { return a.id === docId; });
  if (!acc) return;

  document.getElementById('accountModalTitle').innerText = 'Edit Akun';
  document.getElementById('editAccountId').value = acc.id;
  document.getElementById('accUsername').value = acc.username;
  document.getElementById('accPassword').value = acc.password;
  document.getElementById('accRole').value = acc.role;
  document.getElementById('accountModal').classList.remove('hidden');
}

function closeAccountModal() {
  document.getElementById('accountModal').classList.add('hidden');
}

function handleAccountFormSubmit(event) {
  event.preventDefault();

  var docId = document.getElementById('editAccountId').value;
  var uname = document.getElementById('accUsername').value.trim();
  var pword = document.getElementById('accPassword').value.trim();
  var role = document.getElementById('accRole').value;

  if (!uname || !pword) return;

  var accountData = {
    username: uname,
    password: pword,
    role: role
  };

  if (docId) {
    // Update existing account
    db.collection("accounts").doc(docId).update(accountData)
      .then(function() {
        closeAccountModal();
      })
      .catch(function(err) {
        console.error("Gagal mengupdate akun:", err);
        alert("Gagal mengupdate akun di Firebase.");
      });
  } else {
    // Add new account
    accountData.createdAt = System.currentTimeMillis ? System.currentTimeMillis() : Date.now();
    db.collection("accounts").add(accountData)
      .then(function() {
        closeAccountModal();
      })
      .catch(function(err) {
        console.error("Gagal menambahkan akun:", err);
        alert("Gagal menambahkan akun ke Firebase.");
      });
  }
}

function deleteAccount(docId, username) {
  if (confirm('Apakah Anda yakin ingin menghapus akun "' + username + '"?')) {
    db.collection("accounts").doc(docId).delete()
      .then(function() {
        console.log("Akun berhasil dihapus.");
      })
      .catch(function(err) {
        console.error("Gagal menghapus akun:", err);
        alert("Gagal menghapus akun dari Firebase.");
      });
  }
}

// ==========================================================================
// 4. FIRESTORE DATA LISTENERS (CHATS, GRATITUDES, CRISIS, PERIODS)
// ==========================================================================

function initDataListeners() {
  // Listen to CHATS
  db.collection("chats").onSnapshot(function(snapshot) {
    allChats = [];
    snapshot.forEach(function(doc) {
      allChats.push(Object.assign({ id: doc.id }, doc.data()));
    });
    processDataAndRender();
  }, function(err) {
    console.error("Chats Listener Error:", err);
  });

  // Listen to GRATITUDES
  db.collection("gratitudes").onSnapshot(function(snapshot) {
    allGratitudes = [];
    snapshot.forEach(function(doc) {
      allGratitudes.push(Object.assign({ id: doc.id }, doc.data()));
    });
    processDataAndRender();
  }, function(err) {
    console.error("Gratitudes Listener Error:", err);
  });

  // Listen to CRISIS LOGS
  db.collection("crisis_logs").onSnapshot(function(snapshot) {
    allCrisisLogs = [];
    snapshot.forEach(function(doc) {
      allCrisisLogs.push(Object.assign({ id: doc.id }, doc.data()));
    });
    processDataAndRender();
  }, function(err) {
    console.error("Crisis Logs Listener Error:", err);
  });

  // Listen to PERIODS METADATA
  db.collection("periods").onSnapshot(function(snapshot) {
    allPeriods = [];
    snapshot.forEach(function(doc) {
      allPeriods.push(Object.assign({ id: doc.id }, doc.data()));
    });
    processDataAndRender();
  }, function(err) {
    console.error("Periods Listener Error:", err);
  });
}

function processDataAndRender() {
  usersMap.clear();

  // Aggregate from CHATS
  allChats.forEach(function(chat) {
    var uname = chat.username || 'Teman';
    if (!usersMap.has(uname)) {
      usersMap.set(uname, { username: uname, chats: [], gratitudes: [], crisis: [], periods: [], lastActive: 0 });
    }
    var u = usersMap.get(uname);
    u.chats.push(chat);
    if (chat.timestamp && chat.timestamp > u.lastActive) {
      u.lastActive = chat.timestamp;
    }
  });

  // Aggregate from GRATITUDES
  allGratitudes.forEach(function(g) {
    var uname = g.username || 'Teman';
    if (!usersMap.has(uname)) {
      usersMap.set(uname, { username: uname, chats: [], gratitudes: [], crisis: [], periods: [], lastActive: 0 });
    }
    var u = usersMap.get(uname);
    u.gratitudes.push(g);
    if (g.timestamp && g.timestamp > u.lastActive) {
      u.lastActive = g.timestamp;
    }
  });

  // Aggregate from CRISIS LOGS
  allCrisisLogs.forEach(function(c) {
    var uname = c.username || 'Teman';
    if (!usersMap.has(uname)) {
      usersMap.set(uname, { username: uname, chats: [], gratitudes: [], crisis: [], periods: [], lastActive: 0 });
    }
    var u = usersMap.get(uname);
    u.crisis.push(c);
    if (c.timestamp && c.timestamp > u.lastActive) {
      u.lastActive = c.timestamp;
    }
  });

  // Aggregate from PERIODS
  allPeriods.forEach(function(p) {
    var uname = p.username || 'Teman';
    if (!usersMap.has(uname)) {
      usersMap.set(uname, { username: uname, chats: [], gratitudes: [], crisis: [], periods: [], lastActive: 0 });
    }
    var u = usersMap.get(uname);
    u.periods.push(p);
  });

  renderUserGrid();

  if (loggedInAccount && loggedInAccount.role === 'developer') {
    renderDeveloperStats();
    renderErrorNotifications();
  }

  if (activeUser && usersMap.has(activeUser.username)) {
    activeUser = usersMap.get(activeUser.username);
    renderUserDetailView();
  }
}

// ==========================================================================
// 5. USER GRID & SEARCH
// ==========================================================================

function renderUserGrid() {
  var grid = document.getElementById('usersGrid');
  var searchInput = document.getElementById('userSearchInput');
  var searchVal = (searchInput ? searchInput.value : '').toLowerCase();
  grid.innerHTML = '';

  var usersArray = Array.from(usersMap.values());
  var filtered = usersArray.filter(function(u) {
    return u.username.toLowerCase().indexOf(searchVal) !== -1;
  });

  if (filtered.length === 0) {
    grid.innerHTML = '<div class="empty-state"><i class="fa-solid fa-users-slash"></i><p>Belum ada data pengguna yang tersimpan di Firebase Firestore.</p></div>';
    return;
  }

  filtered.forEach(function(u) {
    var initial = u.username.charAt(0).toUpperCase();
    var lastActiveDate = u.lastActive 
      ? new Date(u.lastActive).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
      : '-';

    var hasWarning = u.crisis.length > 0;
    var warningBadge = hasWarning 
      ? '<span class="card-warning-tag"><i class="fa-solid fa-triangle-exclamation"></i> ' + u.crisis.length + ' Warning</span>'
      : '<span style="color:#10B981;"><i class="fa-solid fa-circle-check"></i> Aman</span>';

    var card = document.createElement('div');
    card.className = 'user-card';
    card.onclick = function() { openUserDetail(u.username); };
    card.innerHTML = 
      '<div class="user-card-header">' +
        '<div class="avatar-circle">' + initial + '</div>' +
        '<div>' +
          '<div class="user-card-name">' + u.username + '</div>' +
          '<div class="user-card-date">Aktif: ' + lastActiveDate + '</div>' +
        '</div>' +
      '</div>' +
      '<div class="user-card-metrics">' +
        '<span>💬 ' + u.chats.length + ' Pesan</span>' +
        '<span>🌸 ' + u.gratitudes.length + ' Gratitude</span>' +
        warningBadge +
      '</div>';
    grid.appendChild(card);
  });
}

function filterUsers() {
  renderUserGrid();
}

function navToUserList() {
  activeUser = null;
  document.getElementById('userListView').classList.remove('hidden');
  document.getElementById('userDetailView').classList.add('hidden');
}

function openUserDetail(username) {
  activeUser = usersMap.get(username);
  if (!activeUser) return;

  document.getElementById('userListView').classList.add('hidden');
  document.getElementById('userDetailView').classList.remove('hidden');

  populatePeriodFilter();
  renderUserDetailView();
}

// ==========================================================================
// 6. USER DETAIL VIEW & 15-DAY PERIOD FILTERING
// ==========================================================================

function populatePeriodFilter() {
  var select = document.getElementById('userPeriodFilter');
  if (!select || !activeUser) return;

  var currentVal = select.value || 'all';
  select.innerHTML = '<option value="all">Semua Periode (Gabungan)</option>';

  var periodsSet = {};

  activeUser.chats.forEach(function(c) { if (c.periodId) periodsSet[c.periodId] = true; });
  activeUser.gratitudes.forEach(function(g) { if (g.periodId) periodsSet[g.periodId] = true; });
  activeUser.crisis.forEach(function(cr) { if (cr.periodId) periodsSet[cr.periodId] = true; });
  activeUser.periods.forEach(function(p) { if (p.periodId) periodsSet[p.periodId] = true; });

  var sortedPeriods = Object.keys(periodsSet).sort();
  if (sortedPeriods.length === 0) {
    var opt = document.createElement('option');
    opt.value = "periode_1";
    opt.innerText = "Periode 1 (Default)";
    select.appendChild(opt);
  } else {
    sortedPeriods.forEach(function(p) {
      var opt = document.createElement('option');
      opt.value = p;
      var numStr = p.replace("periode_", "");
      opt.innerText = "Periode " + numStr + " (15 Harian)";
      select.appendChild(opt);
    });
  }

  select.value = currentVal;
}

function onPeriodFilterChanged() {
  renderUserDetailView();
}

function renderUserDetailView() {
  if (!activeUser) return;

  var selectedPeriod = document.getElementById('userPeriodFilter') ? document.getElementById('userPeriodFilter').value : 'all';

  var filteredChats = activeUser.chats.slice();
  var filteredCrisis = activeUser.crisis.slice();
  var filteredGratitudes = activeUser.gratitudes.slice();

  if (selectedPeriod !== 'all') {
    filteredChats = filteredChats.filter(function(c) { return (c.periodId || 'periode_1') === selectedPeriod; });
    filteredCrisis = filteredCrisis.filter(function(cr) { return (cr.periodId || 'periode_1') === selectedPeriod; });
    filteredGratitudes = filteredGratitudes.filter(function(g) { return (g.periodId || 'periode_1') === selectedPeriod; });
  }

  document.getElementById('detailUserAvatar').innerText = activeUser.username.charAt(0).toUpperCase();
  document.getElementById('detailUserName').innerText = activeUser.username;
  
  var lastActiveStr = activeUser.lastActive 
    ? new Date(activeUser.lastActive).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    : '-';
  document.getElementById('detailUserSub').innerText = 'Terakhir Aktif: ' + lastActiveStr;

  document.getElementById('detailChatCount').innerText = filteredChats.length;
  document.getElementById('detailWarningCount').innerText = filteredCrisis.length;
  document.getElementById('detailGratitudeCount').innerText = filteredGratitudes.length;

  var warningBadge = document.getElementById('warningTabBadge');
  if (filteredCrisis.length > 0) {
    warningBadge.innerText = filteredCrisis.length;
    warningBadge.classList.remove('hidden');
  } else {
    warningBadge.classList.add('hidden');
  }

  populateChatDateFilter(filteredChats);
  renderUserChats(filteredChats);
  renderUserWarnings(filteredCrisis);
  renderUserGratitudes(filteredGratitudes);

  // Control visibility of the Delete User Database Button (Developer Only)
  var deleteUserBtn = document.getElementById('btnDeleteUserDb');
  if (deleteUserBtn) {
    if (loggedInAccount && loggedInAccount.role === 'developer') {
      deleteUserBtn.classList.remove('hidden');
    } else {
      deleteUserBtn.classList.add('hidden');
    }
  }
}

function switchTab(tabId) {
  var btns = document.querySelectorAll('.tab-btn');
  for (var i = 0; i < btns.length; i++) {
    btns[i].classList.remove('active');
  }
  var contents = document.querySelectorAll('.tab-content');
  for (var j = 0; j < contents.length; j++) {
    contents[j].classList.remove('active');
  }

  if (window.event && window.event.currentTarget) {
    window.event.currentTarget.classList.add('active');
  }
  document.getElementById(tabId).classList.add('active');
}

// TAB 1: CHATS
function populateChatDateFilter(chatsList) {
  var select = document.getElementById('chatDateFilter');
  if (!select) return;
  select.innerHTML = '<option value="all">Semua Tanggal</option>';

  var datesMap = {};
  (chatsList || activeUser.chats).forEach(function(c) {
    if (c.date) datesMap[c.date] = true;
  });

  var sortedDates = Object.keys(datesMap).sort().reverse();
  sortedDates.forEach(function(d) {
    var opt = document.createElement('option');
    opt.value = d;
    opt.innerText = d;
    select.appendChild(opt);
  });
}

function renderUserChats(chatsList) {
  var container = document.getElementById('chatMessagesContainer');
  var selectedDate = document.getElementById('chatDateFilter').value;
  container.innerHTML = '';

  var chats = (chatsList || activeUser.chats).slice();
  if (selectedDate !== 'all') {
    chats = chats.filter(function(c) { return c.date === selectedDate; });
  }

  chats.sort(function(a, b) { return (a.timestamp || 0) - (b.timestamp || 0); });

  if (chats.length === 0) {
    container.innerHTML = '<div class="empty-state"><i class="fa-solid fa-comments"></i><p>Tidak ada riwayat chat untuk periode/tanggal ini.</p></div>';
    return;
  }

  var currentDate = '';
  chats.forEach(function(msg) {
    if (msg.date && msg.date !== currentDate) {
      currentDate = msg.date;
      var divider = document.createElement('div');
      divider.className = 'date-divider';
      divider.innerHTML = '<span>📅 ' + currentDate + '</span>';
      container.appendChild(divider);
    }

    var row = document.createElement('div');
    row.className = 'chat-bubble-row ' + (msg.isUser ? 'user' : 'bot');
    
    var timeStr = msg.timestamp 
      ? new Date(msg.timestamp).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) 
      : '';

    row.innerHTML = 
      '<span class="chat-sender-label">' + (msg.isUser ? activeUser.username : 'ILU (AI)') + (timeStr ? ' • ' + timeStr : '') + '</span>' +
      '<div class="chat-bubble">' + msg.text + '</div>';
    container.appendChild(row);
  });
}

// TAB 2: WARNINGS
function renderUserWarnings(crisisList) {
  var container = document.getElementById('warningListContainer');
  container.innerHTML = '';

  var warnings = (crisisList || activeUser.crisis).slice().sort(function(a, b) { return (b.timestamp || 0) - (a.timestamp || 0); });

  if (warnings.length === 0) {
    container.innerHTML = '<div class="empty-state"><i class="fa-solid fa-shield-halved" style="color:#10B981;"></i><p style="color:#10B981; font-weight:600;">Tidak Ada Kejadian Krisis</p><p>Pengguna ini aman dan tidak pernah memicu frasa krisis pada periode ini.</p></div>';
    return;
  }

  warnings.forEach(function(w) {
    var card = document.createElement('div');
    card.className = 'warning-card';
    var isReviewed = w.reviewed === true;

    card.innerHTML = 
      '<div class="warning-info">' +
        '<div class="warning-title"><i class="fa-solid fa-circle-exclamation"></i> Terdeteksi Kata Kunci Krisis</div>' +
        '<div class="warning-meta">' +
          'Waktu: <strong>' + (w.date || '-') + '</strong> | ' +
          'Kata Pemicu: <span class="trigger-tag">' + (w.triggerKeyword || 'N/A') + '</span> | ' +
          'Siklus: <strong>' + (w.periodId ? w.periodId.replace('_', ' ') : 'periode 1') + '</strong>' +
        '</div>' +
      '</div>' +
      '<div>' +
        '<button class="btn-review ' + (isReviewed ? 'done' : 'pending') + '" onclick="markReviewed(\'' + w.id + '\', ' + !isReviewed + ')">' +
          (isReviewed ? '<i class="fa-solid fa-check"></i> Sudah Ditinjau' : '<i class="fa-solid fa-bell"></i> Tandai Sudah Ditinjau') +
        '</button>' +
      '</div>';
    container.appendChild(card);
  });
}

function markReviewed(docId, newStatus) {
  db.collection("crisis_logs").doc(docId).update({ reviewed: newStatus })
    .then(function() {
      console.log("Status review berhasil diupdate.");
    })
    .catch(function(err) {
      console.error("Gagal mengupdate status review:", err);
      alert("Gagal mengupdate status di Firebase.");
    });
}

// TAB 3: GRATITUDES
function renderUserGratitudes(gratitudesList) {
  var container = document.getElementById('gratitudeListContainer');
  container.innerHTML = '';

  var gratitudes = (gratitudesList || activeUser.gratitudes).slice().sort(function(a, b) { return (b.timestamp || 0) - (a.timestamp || 0); });

  if (gratitudes.length === 0) {
    container.innerHTML = '<div class="empty-state"><i class="fa-solid fa-heart-crack"></i><p>Belum ada data 3 Hal Baik yang tersimpan pada periode ini.</p></div>';
    return;
  }

  gratitudes.forEach(function(g) {
    var card = document.createElement('div');
    card.className = 'gratitude-card';
    card.innerHTML = 
      '<div class="gratitude-card-header">' +
        '<span class="gratitude-date"><i class="fa-solid fa-calendar-day"></i> ' + g.date + '</span>' +
      '</div>' +
      '<div class="gratitude-items">' +
        '<div class="gratitude-item"><span class="gratitude-item-num">1</span><span>' + (g.good1 || '-') + '</span></div>' +
        '<div class="gratitude-item"><span class="gratitude-item-num">2</span><span>' + (g.good2 || '-') + '</span></div>' +
        '<div class="gratitude-item"><span class="gratitude-item-num">3</span><span>' + (g.good3 || '-') + '</span></div>' +
      '</div>';
    container.appendChild(card);
  });
}

// Developer Stats
function renderDeveloperStats() {
  document.getElementById('statTotalChats').innerText = allChats.length;
  document.getElementById('statTotalUsers').innerText = usersMap.size;
  document.getElementById('statTotalWarnings').innerText = allCrisisLogs.length;
  document.getElementById('statTotalGratitudes').innerText = allGratitudes.length;

  var dateCounts = {};
  allChats.forEach(function(c) {
    var d = c.date || 'Lainnya';
    dateCounts[d] = (dateCounts[d] || 0) + 1;
  });

  var sortedDates = Object.keys(dateCounts).sort();
  var counts = sortedDates.map(function(d) { return dateCounts[d]; });

  var canvas = document.getElementById('dailyChatChart');
  if (!canvas) return;
  var ctx = canvas.getContext('2d');
  if (dailyChartInstance) {
    dailyChartInstance.destroy();
  }

  dailyChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: sortedDates.length > 0 ? sortedDates : ['Belum Ada Data'],
      datasets: [{
        label: 'Jumlah Pesan Chat',
        data: counts.length > 0 ? counts : [0],
        backgroundColor: '#F5C518',
        borderColor: '#D4A000',
        borderWidth: 1,
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
    }
  });
}

function renderErrorNotifications() {
  var container = document.getElementById('errorNotificationsList');
  if (!container) return;
  container.innerHTML = '';

  var alerts = [];
  var unreviewedCount = allCrisisLogs.filter(function(c) { return !c.reviewed; }).length;
  if (unreviewedCount > 0) {
    alerts.push({
      title: '🚨 Peringatan Krisis Belum Ditinjau',
      desc: 'Terdapat ' + unreviewedCount + ' kejadian krisis yang membutuhkan peninjauan pihak rumah sakit.'
    });
  }

  usersMap.forEach(function(u) {
    if (u.crisis.length >= 2) {
      alerts.push({
        title: '⚠️ Perhatian Khusus: User ' + u.username,
        desc: 'Pengguna ini telah memicu ' + u.crisis.length + ' kali peringatan krisis.'
      });
    }
  });

  if (alerts.length === 0) {
    container.innerHTML = '<div style="color: #059669; font-size: 13px; padding: 12px; background: #ECFDF5; border-radius: 8px;"><i class="fa-solid fa-circle-check"></i> Tidak ada bug atau error terdeteksi pada sistem.</div>';
    return;
  }

  alerts.forEach(function(a) {
    var item = document.createElement('div');
    item.className = 'error-item';
    item.innerHTML = '<div class="error-item-title">' + a.title + '</div><div class="error-item-desc">' + a.desc + '</div>';
    container.appendChild(item);
  });
}

// ==========================================================================
// 12. DELETE USER DATABASE (DEVELOPER ONLY)
// ==========================================================================
function confirmDeleteUserDatabase() {
  if (!activeUser) return;

  var uname = activeUser.username;
  if (!confirm("Apakah Anda yakin ingin menghapus seluruh data database (riwayat chat, gratitude, log krisis, dan data periode) untuk pengguna '" + uname + "' secara permanen?\n\nTindakan ini akan menghapus semua records dari Firebase Firestore.")) {
    return;
  }

  if (!confirm("PENTING: Tindakan ini tidak bisa dibatalkan dan semua data pengguna '" + uname + "' di Firebase akan hilang selamanya. Lanjutkan?")) {
    return;
  }

  var deleteBtn = document.getElementById('btnDeleteUserDb');
  var originalHtml = deleteBtn.innerHTML;
  deleteBtn.disabled = true;
  deleteBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Menghapus...';

  var collections = ["chats", "gratitudes", "crisis_logs", "periods"];
  var deletePromises = [];

  collections.forEach(function(col) {
    var p = db.collection(col).where("username", "==", uname).get().then(function(querySnapshot) {
      var batch = db.batch();
      querySnapshot.forEach(function(doc) {
        batch.delete(doc.ref);
      });
      return batch.commit();
    });
    deletePromises.push(p);
  });

  Promise.all(deletePromises).then(function() {
    alert("Data database pengguna '" + uname + "' berhasil dihapus secara permanen.");
    deleteBtn.disabled = false;
    deleteBtn.innerHTML = originalHtml;
    navToUserList();
  }).catch(function(error) {
    console.error("Gagal menghapus data pengguna:", error);
    alert("Terjadi kesalahan saat menghapus data dari Firebase: " + error.message);
    deleteBtn.disabled = false;
    deleteBtn.innerHTML = originalHtml;
  });
}
