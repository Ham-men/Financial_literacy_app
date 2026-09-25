# AGENTS.md — Питомец Финни (Financial Literacy App v5.0)

Авто-документация для AI-агентов и разработчиков. Обновлять при каждом дне плана.

## 1. Инициализация (Текущее состояние — v5.0 P0: карта + здание подключены, сборка зелёная)

Стек: Kotlin + Jetpack Compose + Room + DataStore + WorkManager + Navigation Compose.
MinSdk **26** (Android 8.0), targetSdk 34+, namespace `com.example.financialliteracyapp`.

**Что уже есть (MVP ядро v3.0):**
- Питомец (PetScreen + PetViewModel + PetDecayWorker 1h) — эмодзи по шкалам
- Банки (BankScreen + BankViewModel) — DnD 5×100 ₡ → WalletEntity
- Ларёк (ShopScreen + ShopViewModel) — склад, цена, прогноз BotBrain
- 2 мини-игры + живая касса: SuppliersGame (закупка), PricerGame (ценник), CashierScene (живая продажа на кассе)
- 20 ботов WORKER — simulateBotDay() через EconomicEngine + BotBrain
- Журнал (JournalScreen) + Отчёт (ReportScreen) — реальные TransactionEntity
- Room: PetEntity, WalletEntity, ShopEntity, TransactionEntity, BotEntity
- DataStore: UserPrefs (petName, sound, difficulty, onboardingDone, currentDay)

**Сборка:** `assembleDebug` — BUILD SUCCESSFUL (JDK 21 JBR).

---

## 2. Новый план — Сюжет v5.0: Единая самоподдерживающаяся инфраструктура

**Главный принцип:** Одно здание = рабочее место + точка продажи одновременно. Нет отдельных «работодателей» и «магазинов».
**Замкнутый цикл:** боты работают в зданиях игрока → получают зарплату → тратят в тех же зданиях → деньги возвращаются игроку.

**5 типов зданий (каждое — и работа, и магазин):**
1. 🛒 **Продукты** — нанимает кассиров/закупщиков/уборщиков, продаёт еду всем ботам
2. 🔧 **СТО** — нанимает механиков, ремонт машин (только боты с 🚗)
3. 🏗️ **Стройтехника** — нанимает кладовщиков/грузчиков, продаёт инструменты/материалы
4. 🏥 **Здоровье** — нанимает врачей/фармацевтов, лекарства/приём/скорая
5. 🎨 **Искусство** — нанимает кураторов/художников, картины/выставки (элита)
6. 🏠 **Жилой дом** (особый) — сдаёт квартиры в аренду, пассивный доход

**MVP приоритеты (сюжет5.txt §13):**
- P0: Top-down карта одного района + перемещение Финни (джойстик) + одно здание «Продукты» (найм + продажа) + 5 ботов работают и покупают
- P1: Мини-игры (закупка, касса), зарплата ботам, траты ботов в здании, отчёт дня
- P2: Второе здание (СТО), боты с машинами, улучшения, найм сотрудников
- P3: Жилой дом (аренда), второй район, конкуренты

---

## 3. Прогресс по новому плану v5.0

### Этап 0. Подготовка к v5.0
- [x] MinSdk 26 в gradle
- [ ] Аудит текущих сущностей под v5.0 (BuildingEntity, BuildingType, EmployeeRole, BotType с машиной)
- [ ] Матрица требований от сюжет5.txt

### Итерации «все приложение в целом» (сцены/логика по описаниям пользователя)
- [x] **Сцена ДОМ + ЕДА:** в MainScreen (ДОМ) добавлен уровень (уровень/стадия выводится в параметрах, petEmoji по состоянию), кнопки «🍖 Покормить — 20₡» (FEED_COST) и «💊 Лечение — 30₡» (HEAL_COST) перенесены из FoodScreen. Деньги списываются из банки «нужное» (spendFromNeed: needPlan → needFact, фолбэк на мешок). Сцена «ЕДА» удалена: Routes.FOOD, composable в NavGraph, NavItem в NavSidebar, FoodScreen.kt. repo.healPet() — +30 настроение, транзакция pet_heal.
- [x] **Сцена КАРТА (покупка магазинов):** Покупка участка больше НЕ сводится к флагам в DataStore — теперь создаётся настоящее рабочее здание (BuildingEntity с plotId, DB v5 + MIGRATION_4_5: ALTER buildings ADD plotId). Покупка: LotScreen → repo.buyShop(plotId, type) (списывает LOT_PRICE, upsert здания с дефолтными stock/price/costPrice, транзакция EXPENSE lot). Клик по купленному магазину на карте открывает BUILDING/{id} (интерьер как у обычных магазинов: персонал, товар, касса, найм). Продажа: sellShop(plotId) → +LOT_SELL_PRICE, deleteByPlotId, INCOME lot_sale. Пассивного дохода от купленных магазинов НЕТ: убраны payLotRent/LOT_RENT_PER_DAY/лот-префсы DataStore (lotPurchased/lotType/lotPlotId), строка «Доход от участка» и lot_rent из KNOWN_INCOME_SOURCES и тестов.
- [x] **Связка финансов План→сцены (банки = источники трат):** Все доходы в «💼 Мешок» (cash), распределение — только в BankScreen (moveBagToBank / withdrawAllToBag / копилка 5%/день). Траты привязаны к банкам: «нужное» (spendFromNeed: needPlan→needFact, фолбэк на мешок при нераспределённом плане) — корм (ДОМ), лечение (ДОМ), закупка товара (purchaseStockForBuilding), найм 50₡ (hireBotToBuilding), зарплаты (paySalaryToBot, payCashierSalary, simulateDay), покупка магазина (buyShop LOT_PRICE). «Желаемое» (spendFromWant: wantPlan→wantFact) — игрушки для Финни (buyCatalogItem WANT). «Копилка» (savePlan) НЕ тратится — только копится (проценты) и откладывается в цели (addToGoal). Обобщённый `spendFromPlan(useNeed, cost)` в GameRepository — единая точка списания по банкам.
- [x] **Сцена КОМНАТА РАЗВЛЕЧЕНИЙ (игрушки → опыт → уровень → ставка копилки):** Новый маршрут ENTERTAINMENT + пункт «🧸 ИГРЫ» в NavSidebar + EntertainmentScreen/EntertainmentViewModel. В комнате: уровень развлечений Финни (1..5, 5 опыта = 1 уровень, GameRules.playLevel), ставка копилки = SAVE_INTEREST_PERCENT 5% + (уровень−1) (GameRules.playInterestPercent, максимум 9% на 5 уровне), список игрушек WANT из каталога (описание + «Опыт +N» + цена), секция заданий PLAY с прогрессом, покупка через buyCatalogItem (тратит «желаемое»). При покупке игрушки: pet.xp += xpReward (потолок 25), mood, транзакция shop_purchase, авто-завершение PLAY-заданий с наградой (quest_reward) через updatePlayQuests. resetDay() теперь считает проценты по ставке от уровня развлечений (pet.xp). DB v6 + MIGRATION_5_6: pet.xp, catalog_items.xpReward/description (бекап старых установок SQL UPDATE по названию игрушек). Каталог WANT: Мячик(+3 опыта), Бантик(+2), Картина(+4), Торт(+5). PLAY-задания: «Первая игрушка» (3 опыта, 50₡), «Уровень 2» (10 опыта, 70₡), «Уровень 5» (25 опыта, 120₡) — добавляются и для старых БД (секция ensureSeed else-if). QuestsScreen/ProgressScreen: иконка PLAY 🧸.
- [x] **Сцена ИГРОВОЕ ВРЕМЯ (часы + HUD + сон):** `GameClock` (data/clock) — 1 игр.минута = 1 реальная секунда, время добегает при закрытом приложении (gameMinute + gameClockEpoch в DataStore). Рабочий день 10:00–18:00 (GameRules.isShopOpen): после 18:00 магазины закрыты, боты не заходят (BuildingCashierScreen останавливает спавн, BuildingInteriorScreen показывает «🔴 Закрыто — после 18:00»). `TimeBankBar` — глобальный HUD в NavGraph над содержимым всех сцен: дата (день 1 = 01.01.2020), время, 3 банки с зелёной подсветкой тратной банки по route (`spendBankFor`). Сон: кровать 🛏️ на MainScreen (кликабельна) или нижний баннер «пора спать» в 23:00; спать можно 18:00–23:00 (GameRules.canSleep), день+1 → GameClock.newDay() (10:00), payCashierSalary + resetDay в MainViewModel.sleep()/ReportViewModel.nextDay.

### Этап 1. Неделя 1 — Ядро мира P0 (Карта + Финни + одно здание Продукты)
- [x] **День 1:** Top-down карта на Compose (один район «Рынок»): дороги, живое здание «Продукты», живой дом. MapScreen (моки) + HUD + маршрут MAP. (Навигация из плитки «Мир».)
- [x] **День 2:** Перемещение Финни — виртуальный джойстик (левый) + тап-по-точке. Вход в здание тапом по зданию → BUILDING/{id}. Коллизии пока упрощены.
- [x] **День 3:** Сцена внутри здания: BuildingInteriorScreen (горячие точки: закупка, полки, ценник, касса, найм, улучшения, учёт), работает с BuildingEntity из БД (stock/price/costPrice/soldToday). Под-экраны подключены маршрутами SUPPLIERS/PRICER/HIRE/CASHIER.
- [x] **День 4:** Боты-NPC на карте: 5 ботов из БД ходят (Animatable) дом↔март, заходят в здание, работают, покупают, выходят. Визуально: спрайт по типу (🚶/🚗/🕴️/…), над головой статус (🛠️/🛍️/🏠). Позиция/состояние пишутся в БД через repo.updateBotStatePosition.
- [x] **День 5:** Жизненный цикл бота в здании: визуальный цикл на карте + экономика simulateDay() (зарплата→траты→выручка). Боты привязаны к зданию (workBuildingId); в здании кнопка «🚀 Запустить день» с итогом (ЗП/выручка/себест./аренда/налог/прибыль).
- [x] **День 6:** Зарплата: в BuildingInteriorScreen карточка «👩‍💼 Сотрудники и зарплата» с кнопкой «ЗП N ₡» на бота (списывается из Wallet → wallet бота → бот может тратить). В конце дня зарплаты идут автоматически в simulateDay().
- [x] **День 7:** Траты ботов: BuildingCashierScreen — боты (покупатели из BotBrain.estimateBuyers) идут дверь→полка (stock-- через takeItemFromBuilding)→касса; игрок пробивает (CashierScene: тащит товар, выдаёт сдачу), completeSaleInBuilding пишет выручку/транзакции; при выходе необслуженный товар возвращается (refundStockToBuilding).

### Этап 2. Неделя 2 — Мини-игры и цикл P1
- [x] **День 8:** Закупка товара внутри здания: SuppliersGame → склад здания «Продукты» (покупка через purchaseStockForBuilding).
- [x] **День 9:** Расстановка товара на полки: ShelvesGame адаптирован на здание (observeShop → здание PRODUCTS).
- [x] **День 10:** Ценник: PricerGame — слайдер цены + прогноз спроса, пишет цену здания.
- [x] **День 11:** Касса: живая касса здания (CashierScene в BuildingCashierScreen) — игрок тащит товар, бот платит, игрок выдаёт сдачу.
- [x] **День 12:** Уборка: CleaningGameScreen (tap-to-clean, 4×4 тайла) — грязь накапливается со временем (spawner), пишется в dirtLevel (repo.updateDirtLevel), награда +40 ₡ (repo.rewardCleaning), при грязи >50 в интерьере предупреждение.
- [x] **День 13:** Отчёт дня: ReportScreen (выручка/зарплаты/закупки/прибыль по транзакциям). «Следующий день» → currentDay+1, resetDay() (сброс soldToday, проценты копилки save_interest).
- [x] **День 14:** Найм сотрудников: карточка «👤 Нанять на работу» в интерьере — слайдер зарплаты 50–300 (бот соглашается при ≥100), hireBotToBuilding (workBuildingId + разовый сбор 50 ₡). Нанятый попадает в «Сотрудники и зарплата» и в simulateDay.
- [x] **Связь денег (мешок ↔ банки):** Все доходы (ларек/СТО/стройка/пассивно) идут в `cash` = «💼 Мешок». BankScreen заново: монеты по 50 ₡ (+остаток) в мешке → тап монеты → тап банки (карман/желаемое/копилка) → moveBagToBank. Кнопка «🎒 Вывести всё в мешок» → withdrawAllToBag (все банки → cash). Копилка — накопительный счёт: в resetDay проценты (5%/день, GameRules.saveInterest) возвращаются в банку копилка (транзакция save_interest). resetDay больше НЕ сбрасывает банки в cash (перераспределение — кнопкой).

### Этап 3. Неделя 3 — Расширение P2 (СТО + машины + улучшения)
- [x] **День 15:** Второе здание: СТО. Сид AUTO_SERVICE (Рынок, x13 y5, stock30 price30 cost12). Инженер-механик 🚗 работает там; покупают в СТО только боты с машиной (simulateDay фильтрует hasCar). Карта: бот идёт в своё рабочее здание или водитель — в СТО, остальные — в Продукты. Интерьер СТО: без «Закупка/Полки/Ценник» (самопрайс 💲), подъёмник 🚗🛠️ вместо полок, касса/уборка/найм/день общие.
- [ ] **День 16:** Боты с машинами: 30% рабочих, 70% инженеров, 90% менеджеров, 100% элиты. Ездят по дорогам на карте.
- [ ] **День 17:** Уникальное действие СТО: мини-игра «Ремонт машины» (тайминг + выбор инструментов).
- [ ] **День 18:** Улучшения зданий: кнопка «🛒 Улучшения» со списком апгрейдов уникальных для типа (полки, холодильники, стойки, инструменты и т.д.).
- [ ] **День 19:** Внутренние роли: кассир (авто-касса), закупщик (авто-закупка), уборщик (авто-уборка), мерчендайзер (авто-полки), механик (авто-ремонт).
- [ ] **День 20:** Зарплата сотрудников: ежедневное списание. Бот может уволиться, если зарплата задержана 3 дня.
- [ ] **День 21:** Вертикальная интеграция: бот из «Продуктов» тратит в СТО → СТО нанимает → тратит в Здоровье → Здоровье нанимает → тратит в Продуктах.

### Этап 4. Неделя 4 — Мир P3 + Надёжность
- [ ] **День 22:** Жилой дом: покупка участка → строительство → заселение ботов за аренду. Пассивный доход.
- [ ] **День 23:** Второй район: переход между районами на карте. Конкуренты (чужие здания с теми же механиками).
- [ ] **День 24:** 6 типов ботов: Рабочий/Инженер/Менеджер/Пенсионер/Студент/Элита с разным доходом, шансом машины, предпочтениями.
- [ ] **День 25:** Стратегия игрока: баланс зарплата ↔ цены, размещение зданий, репутация (оплата вовремя).
- [ ] **День 26-27:** Unit-тесты (план ≤ бюджет, нет минуса, замкнутый цикл зарплата→траты, аренда, найм).
- [ ] **День 28:** Прогон на телефоне, звук/анимации в настройках, README, release APK, иконка 512, 3 скрина.

---

## 4. Контракты данных v5.0 (новые/обновлённые)

- `GameClock` (data/clock) — игровые часы: 1 игровая минута = 1 реальная секунда. День начинается в 10:00 (WORK_DAY_START_MINUTE=600), рабочий день 10:00–18:00 (isShopOpen), спать можно 18:00–23:00 (canSleep), в 23:00 (BED_TIME_MINUTE=1380) время останавливается (isBedtime) и на нижнем баннере появляется «пора ложиться спать». Данные (gameMinute + gameClockEpoch) хранятся в DataStore; при старте время добегает по реальным секундам, новый день — `GameClock.newDay()` → 10:00.
- `TimeBankBar` (ui/components) — глобальный HUD на всех сценах: дата (dateForDay, день 1 = 01.01.2020), время (timeLabel), 3 банки (нужное/желаемое/копилка). Зелёным подсвечивается банка, тратная на текущей сцене (`spendBankFor(route)`): ДОМ→нужное, ИГРЫ→желаемое, ЦЕЛИ→копилка, СТРОЙКА/СТО/ЛАРЁК/здания/закупка/касса→нужное, КАРТА/ПЛАН→без подсветки.
- `UserPrefs` — добавлено `gameMinute`, `gameClockEpoch` (персист часов), `setGameClock(minute, epochMs)`.
- `PetEntity(id=1, name, bodyType:0..2, accessory:0..2, background:0, hunger 0..100, mood, energy, level:1..3, xp (опыт развлечений), x, y)` — позиция на карте
- `WalletEntity(id=1, cash, needPlan, wantPlan, savePlan, needFact, wantFact, saveFact)`
- `BuildingEntity(id, type:PRODUCTS/AUTO_SERVICE/CONSTRUCTION/HEALTH/ART/RESIDENTIAL, district, x, y, level, stock, price, costPrice, cash, soldToday, revenueTotal, dirtLevel, upgradesBitmask, employeesJson, isOpen)`
- `BotEntity(id, type:WORKER/ENGINEER/MANAGER/RETIREE/STUDENT/ELITE, homeBuildingId, workBuildingId, salary, wallet, hasCar, loyalty, state:HOME/WORKING/SHOPPING/MOVING, targetX, targetY)`
- `TransactionEntity(day, kind, category, amount, source, buildingId)`
- `GoalEntity(id, title, targetAmount, currentAmount, completed)`
- `QuestEntity(id, topic, title, description, reward, completed, day, order, progress, target)` — topic: PLANNING/SAVING/SPENDING/PLAY
- `CatalogItemEntity` added: `description`, `xpReward` (игрушки WANT дают опыт развлечений)
- `UserPrefs` — `petName`, `nickname`, `bodyType`, `accessory`, `background`, `soundEnabled`, `difficulty`, `onboardingDone`, `currentDay`, `demoMode`, `growthStage`, `currentDistrict`

---

## 5. Что править агенту прямо сейчас (P0 — ядро мира)

1. **Room сущности v5.0:** `BuildingEntity`, расширенный `BotEntity` (type, hasCar, homeBuildingId, workBuildingId, state, targetX, targetY), `PetEntity` + x,y
2. **GameRepository:** CRUD для зданий, ботов с позициями, зарплата/траты в закрытом цикле
3. **MapScreen (Compose Canvas):** top-down карта района «Рынок» — сетка 20×15 тайлов, дороги, участки, здания, живые боты, Финни с камерой
4. **Движение Финни:** виртуальный джойстик (JoystickView) + тап-по-точке (A* или прямая линия с коллизиями), вход в здание
5. **BuildingInteriorScreen:** универсальный экран внутри здания (типа BuildingType) с горячими точками: прилавок, полки, касса, склад, дверь, стул найма, кнопка улучшений, учёт
6. **BotAI на карте:** движение по путям (дороги/тротуары), вход в здания, работа, покупки, возврат домой
7. **EconomicEngine v5:** simulateDay() с закрытым циклом — зарплаты → траты в зданиях игрока → выручка → прибыль

---

## 6. Как запускать и проверять

1. Android Studio → Sync Gradle (minSdk 26).
2. Run на эмуляторе/устройстве API 26+.
3. Проверка P0: карта загружается → Финни ходит джойстиком → тап по зданию «Продукты» → вход внутрь → боты ходят на карте → заходят в здание → работают → покупают.
4. Kill process → прогресс на месте (Room + DataStore).
5. Logcat без крашей, Room/DS пишутся.