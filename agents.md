# AGENTS.md — Мини-экономика (Financial Literacy App)

Авто-документация для AI-агентов и разработчиков. Обновлять при каждом дне плана.

## 1. Инициализация (Day 1 — done)

Стек: Kotlin + Jetpack Compose + Room + DataStore + WorkManager + Navigation Compose + Lottie (вариант C — эмодзи до v0.2).
MinSdk 24, targetSdk 36, namespace `com.example.financialliteracyapp`.

Структура:
```
app/src/main/java/com/example/financialliteracyapp/
├── MainActivity.kt                         // seed Room + WorkManager
├── data/
│   ├── AppContainer.kt                     // Service Locator без Hilt
│   ├── GameRepository.kt                   // единый репозиторий MVP
│   ├── local/
│   │   ├── AppDatabase.kt                  // Room v1, 5 таблиц, fallbackToDestructive
│   │   ├── entity/PetEntity, WalletEntity, ShopEntity, TransactionEntity, BotEntity
│   │   └── dao/PetDao, WalletDao, ShopDao, TransactionDao, BotDao
│   └── prefs/UserPrefs.kt                  // DataStore: petName, sound, difficulty, onboardingDone, currentDay
├── domain/economy/
│   ├── Balance.kt                          // константы из сюжет 2.txt ч.3
│   └── BotBrain.kt                         // WTP, shopScore, decidePurchase (WORKER)
├── game/PetDecayWorker.kt                  // Periodic 1h, -5 к шкалам
└── ui/
    ├── theme/Color, Type, Shape, Theme
    ├── components/CommonComponents.kt
    ├── navigation/Routes, NavGraph
    └── screens/
        ├── onboarding/OnboardingScreen
        ├── pet/PetScreen + PetViewModel    // Room + WorkManager, эмодзи C
        ├── banks/BankScreen (+ BankViewModel Day5)
        ├── shop/ShopScreen (+ ShopViewModel Day6)
        ├── minigames/SuppliersGame, PricerGame, CashierGame
        ├── report/ReportScreen
        ├── journal/JournalScreen
        └── quests/QuestsScreen
```

Gradle: `app/build.gradle.kts` — `ksp("androidx.room:room-compiler:2.6.1")`, `gradle/libs.versions.toml` — `ksp = "2.0.21-1.0.25"`. Top-level `build.gradle.kts` — `alias(libs.plugins.ksp) apply false`.

Инициализация в `MainActivity.kt:24-38`: `CoroutineScope(IO).launch { repo.ensureSeed() }` + `PeriodicWorkRequestBuilder<PetDecayWorker>(1, HOURS)` с `ExistingPeriodicWorkPolicy.KEEP`.

Lottie: зависимость оставлена, но `R.raw.pet_idle` не требуется — `PetScreen.kt:27-35` показывает эмодзи по состоянию голода/энергии/настроения. Ассет `res/raw/pet_idle.json` — в v0.2.

## 2. Правила для агентов

- Язык кода/комментов — RU в UI-строках, EN в коде.
- Не создавать новые модули без нужды; расширять `GameRepository` и `Balance`.
- Любая монета/цена/шкала — через `Balance` и `WalletEntity/ShopEntity`, а не локальные `mutableIntStateOf`.
- Сохранение — сразу в Room, чтение — `Flow` + `collectAsState()`. Не терять прогресс при перезапуске.
- Проверять `wallet.cash` перед списанием; логировать транзакции в `TransactionEntity`.
- Bot-формулы — только из `BotBrain.kt`, веса WORKER `0.40/0.05/0.20/0.10/0.20/0.05`, `PURCHASE_THRESHOLD=0.30`.
- Не трогать `MainActivity` package — должен остаться `com.example.financialliteracyapp` для `AndroidManifest.xml:14`.
- При правке UI оставлять `MaterialTheme.colorScheme.background/surface`, `CommonComponents.AppCard/BigActionButton/Chip/StatBar`.
- После правок — Sync Gradle и Run; при ошибке `Unresolved reference 'raw'`/`random` — чинить как в Day 1-2.

## 3. MVP-скоуп (короткий план.txt)

Входит: 1 питомец (Енот), 3 шкалы, 3 действия; 3 банки drag-and-drop; 1 ларёк; 3 мини-игры; 20 ботов WORKER; касса+P&L; цикл день; Room+DataStore; RuStore.
Не входит: ЦБК/акции/кредиты, 5 районов, найм, 5 журналов, ML, кастомизация.

## 4. Прогресс по дням

- [x] День 1 — проект, зависимости, структура.
- [x] День 2 — 5 entities+DAO+DB+DataStore+seed 20 ботов.
- [x] День 3 — PetScreen эмодзи C + 3 шкалы + 3 кнопки.
- [x] День 4 — PetViewModel + PetDecayWorker (1h).
- [x] День 5 — BankScreen → Wallet (drag-and-drop 5×100 ₡, confirm пишет `spend/save/invest` via `BankViewModel` + `GameRepository.distributeBanks()`).
- [x] День 6 — ShopScreen → Shop (склад, ползунок цены → `ShopEntity.price` через `ShopViewModel.setPrice()`, прогноз `BotBrain.estimateBuyers`).
- [x] День 7 — SuppliersGame → закупка (выбор из 3, цена/качество, запись `stock/cash/Transaction` via `purchaseStock()`).
- [x] День 8-10 — PricerGame/CashierGame → Room (`PricerGame` пишет цену, `CashierGame` → `runDay(sold)` + прибыль/налог/аренда).
- [x] День 11 — `EconomicEngine` + `BotBrain` (WTP, priceScore, shopScore, decidePurchase для WORKER).
- [x] День 12-13 — `simulateBotDay()`: 20 ботов решают покупку → обновляют `Shop.stock/soldToday/revenue`, `Wallet.cash`, `Bot.wallet/loyalty`, `Transaction`.
- [x] День 14 — `ReportViewModel.nextDay()`: инкремент `currentDay` в DataStore, `repo.resetDay()` (сброс `soldToday`, пополнение ботов +100 ₡).
- [x] День 15-16 — `ReportScreen`/`JournalViewModel` на реальных `Transaction` из Room (P&L: выручка − себестоимость − аренда − налог = прибыль).
- Сборка: `assembleDebug` BUILD SUCCESSFUL (2026-09-14, JDK 21 JBR, warnings только `Divider → HorizontalDivider` в Journal/Report).

## 5. Контракты данных (MVP)

- `PetEntity(id=1, name, hunger 0..100, mood, energy, level)` — `PetDao.observe()`.
- `WalletEntity(id=1, cash=500, spend, save, invest)` — сумма банок + cash = старт 500 + прибыль.
- `ShopEntity(id=1, stock=100, price=8, costPrice=3, cash=200, soldToday, revenueToday)`.
- `TransactionEntity(day, kind INCOME/EXPENSE, category, amount)`.
- `BotEntity(20×WORKER, salary 800, wallet 200, loyalty 0)`.
- `UserPrefs` — `petName`, `soundEnabled`, `difficulty`, `onboardingDone`, `currentDay`.

## 6. Как запускать

1. Android Studio → Sync Gradle.
2. Run на эмуляторе/устройстве API 24+.
3. Проверить: питомец ест (−15 ₡ +25 голода), играет (−10 ₡ +20 настроения), спит (+40 энергии); перезапуск — данные на месте; Logcat без `R.raw`/`random` ошибок.
4. Следующий шаг — Day 5: перетащить 5 монет в банки, Confirm → `WalletEntity` обновляется.

## 7. Что править агенту дальше

- MVP core готов: питомец + 3 банки + ларёк + 3 мини-игры + 20 ботов + цикл дня + отчёт/журнал.
- Остальное — полировка и бэклог:
  - День 17-18: онбординг (3 экрана), звук, QuestsScreen привязать к реальным данным.
  - День 19-20: тесты экономики (30 дней симуляции), playtest.
  - День 21: иконка, скриншоты, подпись AAB, загрузка в RuStore.
- Не заводить новые зависимости без обсуждения (Hilt/Koin — после MVP).
