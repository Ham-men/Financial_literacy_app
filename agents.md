# AGENTS.md — Питомец Финни (Financial Literacy App v5.0)

Авто-документация для AI-агентов и разработчиков. Обновлять при каждом дне плана.

---

## 0. Что это за игра

**«Питомец Финни»** — мобильный обучающий симулятор финансовой грамотности для детей (Android, Compose, только landscape). Ребёнок в игровой форме учится: зарабатывать, распределять бюджет, копить, тратить осознанно, вести бизнес.

**Роль игрока:** ухаживает за питомцем-енотом Финни (корм, лечение, развлечения, внешний вид) и одновременно развивает «инфраструктуру города» — покупает участки, строит магазины, нанимает ботов-работников и продаёт им товары. Главная цель — построить самоподдерживающуюся экономику: **замкнутый цикл**, в котором боты работают в зданиях игрока → получают зарплату → тратят её в магазинах игрока → деньги возвращаются к игроку.

**Валюта:** ₡. Все заработанные деньги попадают в «💼 Мешок» (cash) и **не тратятся напрямую** — игрок сам распределяет их на сцене ПЛАН в 3 банки:
- 👖 **Карман («нужное»)** — еда, лечение, закупка товара, найм, зарплаты, покупка магазина;
- 🚛 **Желаемое («хочется»)** — игрушки для Финни (даёт опыт развлечений);
- 🐷 **Копилка** — только копится: проценты (5% + уровень развлечений, до 9%) в конце дня и отложение в ЦЕЛИ.

**Игровое время:** 1 игровая минута = 1 реальная секунда; рабочий день 10:00–18:00 (магазины открыты), спать можно 18:00–23:00 (день +1). Время добегает при закрытом приложении.

**Базовые принципы мира v5.0:**
- Одно здание = рабочее место + точка продажи одновременно (нет «работодателей» и «магазинов» по отдельности).
- 6 типов зданий: 🛒 Продукты, 🔧 СТО, 🏗️ Стройтехника, 🏥 Здоровье, 🎨 Искусство, 🏠 Жилой дом (аренда).
- Боты (WORKER/ENGINEER/MANAGER/RETIREE/STUDENT/ELITE) ходят по карте, работают, покупают, имеют машины/привычки.
- Обучение с нуля: онбординг (выбор питомца/имени) → пошаговый оверлей-туториал по всем сценам (повтор на ЧИТАХ).

**Сцены (навигация):** левый сайдбар даёт ДОМ (уход за питомцем), ПЛАН (распределение денег), КАРТА (мир/район), ИГРЫ (игрушки и задания), ЧИТЫ (отладка). Магазины открываются ТОЛЬКО через карту (тап по зданию). Глобальный HUD `TimeBankBar` на всех сценах показывает дату, время и 3 банки.

**Текущее состояние:** MVP ядро v3.0 + сюжет v5.0 P0 (карта с одним ларьком «Продукты», замкнутый цикл, авто-касса по найму, внешний вид питомца). Сборка зелёная.

---

## 1. Инициализация (Текущее состояние — v9, Блок А фаз 1–7 завершён: план-факт, цели, рост, сброс/демо, портрет. Сборка зелёная)

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
- [x] **Сцена UI-РАСКЛАДКА (фикс «UI съехал»):** Причина — в NavGraph контентная Column имела `padding(start=70.dp)` ВДОБАВОК к NavSidebar (70.dp) в Row → контент уезжал на ~140dp вправо и сужался до `ширина−140` (сцены уезжали вниз). Исправление: контентная Column теперь `fillMaxHeight().weight(1f)` (занимает место после сайдбара без двойного сдвига, NavGraph.kt). `MainScreen` получил `verticalScroll` (иначе нижние кнопки обрезались из-за TimeBankBar). `ReferenceScreen` — убран внешний `verticalScroll` (вложенный `LazyColumn(weight)` давал вложенный скролл): скроллит только LazyColumn. Интерьеры `Products/Construction/AutoServiceInteriorScreen` — убраны дублирующие TimeBankBar жёсткие шапки «Дата/банки» и уменьшены фикс. размеры декора (полки 110×92, колонки 108+92dp вместо 150+120, эмодзи 34sp), чтобы сцены влезали. `BankScreen` центрирован (адаптивен), `MapScreen`/`BuildingCashierScreen`/`KioskScreen` — свои скроллы/веса. Сборка и тесты зелёные.
- [x] **Сцена ПЛАН (drag-and-drop монет):** `BankScreen` переписан на перетаскивание: сверху «💼 Мешок» + сумма заработанного (справа). Монеты по 50 ₡ (+остаток) появляются кучкой слева под надписью «Мешок» (до 10 шт., остаток — «+N монет в мешке») и свободно перетаскиваются пальцем по сцене (detectDragGestures + offset-слой поверх всех банок). Банки «в карман 👖» (NEED), «в желаемое 🚛» (WANT), «в копилку 🐷» (SAVE) — зоны-приёмники справа: монета, брошенная на банку, переходит в неё (`moveToBank(bank, coin)`), при наведении банка подсвечивается («сюда»). Области банок берутся через `onGloballyPositioned → boundsInRoot()`; центр монеты = origin оверлей-слоя + стартовая позиция + dragOffset. Старое «жми монету → жми банку» убрано (из-за него «клацаешь на банку — ничего не происходит»). «🎒 Вывести всё в мешок» и «← Назад» остались.
- [x] **Сцена ЗАКУПКА ПО ЗДАНИЯМ (ползунок количества + per-building):** `SuppliersGame` переработан: 2 блока (слева выбор поставщика, справа ползунок количества 0–50 → Ст/Ларёк/Стройка/СТО ведут собственную базу). Список поставщиков слева (🐿 Сорока-Поставщик 2.0₡, 🦫 Бобёр-Завод 3.0₡, 🦊 Лис-Оптовик 2.5₡, качество ★ и риск), справа — слайдер `0f..50f`, итоговая стоимость, кнопка «✅ Купить», показ текущего запаса здания (`На складе: N шт`). Раньше закупка всегда шла в legacy-здание «Продукты» (`legacyBuilding()`): маршрут `Routes.SUPPLIERS` НЕ нёс buildingId. Теперь маршрут `"${Routes.SUPPLIERS}/{buildingId}"` (NavType.LongType, default 0), все 7 точек `onOpenSuppliers` в NavGraph.kt передают id здания (KIOSK → kBuilding.id, BUILDING-интерьеры → buildingId, CONSTRUCTION/AUTO_SERVICE → building.id). Покупка через `ShopViewModel.purchaseStockForBuilding(buildingId, pricePerUnit, units)` → `repo.purchaseStockForBuilding` (списание из «нужное», stock += units, costPrice, транзакция EXPENSE stock_purchase с buildingId); фолбэк на старый `purchaseStock` (legacy Продукты) при buildingId = 0 (например встроенный SuppliersGame в KioskScreen). Показан бюджет «нужное» и запас конкретного магазина.
- [x] **Сцена ЧИТЫ (для теста функционала):** Пункт «🎁 ЧИТЫ» в NavSidebar + маршрут CHEATS + CheatsScreen/CheatsViewModel. Внутри: 3 строки «Добавить деньги в банки» (👖 Карман / 🚛 Желаемое / 🐷 Копилка) — текстовое поле суммы + кнопка «+ в банку» справа (repo.cheatAddToBank: плюсует напрямую в needPlan/wantPlan/savePlan без списания с мешка, транзакций нет). Блок «Установить дату и время»: поля «День» и «Время (ЧЧ:ММ: 600 минут = 10:00)» + кнопка «⏰ Установить» (prefs.setCurrentDay + GameClock.setMinute — чит-установка часов без newDay/resetDay, чтобы проверить закрытие магазинов/сон/отчёт). Плюс карточка текущего состояния (день/время/банки).
- [x] **Сцена ДОМ (компакт + кнопка сна):** элементы подняты к верху (окно 96dp, коврик 120×44, зверёк 32sp, кнопки 44dp, статы в 3 строки). Кровать 🛏️ больше НЕ кликабельна — клик ушёл на заметную кнопку «💤 Спать — новый день» рядом с кроватью (активна 18:00–23:00, серая в другое время).
- [x] **Сцена ПЛАН (доработка):** кнопка «← Назад» удалена (навигация только сайдбаром); «🎒 Вывести всё в мешок» перенесена под надпись «💼 Мешок» вверху слева; банки справа ужаты (padding 6dp) и выровнены по верху — все элементы влезают в landscape без прокрутки. BankScreen(onBack) → BankScreen().
- [x] **Авто-ориентация:** в AndroidManifest `screenOrientation="landscape"` → `sensorLandscape` — экран поворачивается в обе стороны при перевороте телефона.
- [x] **Сцена ОБУЧЕНИЕ (первый запуск):** Оверлей-обучение поверх всех сцен при первом входе в игру (после онбординга, пока `prefs.tutorialDone` = false). `TutorialScenarios.steps` (~29 шагов) — по сценам: ДОМ (шапка/Финни/кнопки ухода/сон/сайдбар), ПЛАН (мешок/монеты/3 банки/вывести), КАРТА (район/магазины/участки/дом), ЛАРЁК (полки/закупка/касса+найм), ЗАКУПКА (поставщики+слайдер), КАССА (покупатели/прилавок), СТРОЙКА, СТО, ИГРЫ (уровень/задания/игрушки). `TutorialOverlay` — затемнение + подсвеченная область + стрелка к региону (Spot: SIDEBAR/HUD/TOP_*/CENTER*/BOTTOM_*) + карточка с текстом и кнопками «← Назад» / «Далее →» / «🎓» (пропустить). Навигация между сценами — авто (LaunchedEffect в NavGraph, для SUPPLIERS/CASHIER подставляется buildingId продукт-здания). Повтор запуска — кнопка «🎓 Повторить обучение» на сцене ЧИТЫ (setTutorialDone(false) + start). New: `ui/tutorial/{TutorialData, TutorialScenarios, TutorialViewModel, TutorialOverlay}`.kt, `UserPrefs.tutorialDone/setTutorialDone`, `CheatsScreen(onRestartTutorial)`. Сцена КАРТА — без ходьбы Финни: только тап по участку (интерьер магазина / покупка участка / дом).
- [x] **Сцена ОНБОРДИНГ (компакт + показ персонажа на ДОМ):** DecisionStep (3 карточки по вертикали, экономичный размер, фоны 18/12/11sp, спейсеры 2–8dp), CustomizeStep (превью слева 130×150 + селекторы «Тело/Аксессуар/Фон» справа в один ряд, боксы 44dp, фоны компактные), NameStep (2 поля + кнопка без лишних спейсеров) — всё влезает в landscape без скролла (убран verticalScroll с корневого Column). ДОМ-сцена: вместо эмодзи-котика теперь рисуется выбранный в онбординге персонаж — фон (🏪/🌳/🌊) как подушка + тело (🦝) + аксессуар (🧣/🧢) на коврике, сверху бейдж настроения (😺/😾/😿/😴/🙀). Данные берутся из PetEntity (bodyType/accessory/background), которые сохраняются при онбординге через repo.upsertPet.
- [x] **Сцена НАЙМ СОТРУДНИКА (per-building авто-касса):** Вместо глобального `cashierHired: Boolean` введён per-building набор `hiredBuildings: Set<Long>` (UserPrefs, DataStore-ключ stringSet `hired_buildings`; легаси-ключ CASHIER/`cashierHired` сохранён). 1 сотрудник на здание (СТО/стройка/ларёк): HireScreen по маршруту `HIRE/{buildingId}` (компактный, без скролла; найм/увольнение без платы, зарплата `Balance.CASHIER_SALARY` 80 ₡ в конце дня `payCashierSalary(count)`, списывается в ReportViewModel/MainViewModel по `hiredCount = hiredBuildings.size`). Интерьеры `Products/Construction/AutoServiceInteriorScreen`: работник НЕ показывается до найма (пустое место 🪑), после найма — эмодзи (🧑💼/👷/👨🔧), кнопка «🧾 Касса» `enabled = !hired` (текст «Касса недоступна»), запускается `AutoCashierAnimation` (ТОЛЬКО визуальная анимация: боты 🤖 дверь → полка → работник → уходят; продажи ведёт фоновая экономика, чтобы не задваивать доход).** **Пассивный доход (учёт в кассе магазина):** новый `game/PassiveSales.kt` (`PassiveSales.start(context)` запускается в NavGraph рядом с GameClock) — фоновая экономика независимо от открытой сцены: каждые `GameRules.AUTO_CASHIER_BOT_EVERY_MINUTES = 5` игровых минут, пока магазин открыт (10:00–18:00), каждое здание с нанятым сотрудником продаёт 1 единицу товара через `repo.passiveSaleInBuilding(buildingId)`. **ВЫРУЧКА копится В КАССЕ КОНКРЕТНОГО ЗДАНИЯ** (`building.cash` += цена, `stock`−1, `soldToday`/`revenueTotal` += цена, транзакции INCOME `bot_sales` / EXPENSE `cogs`) и НЕ попадает в мешок игрока сама. Забрать: кнопка «💰 Забрать деньги — N ₡» на `HireScreen` → `repo.withdrawBuildingCash(buildingId)` переводит всю кассу здания в «💼 Мешок» (wallet.cash, транзакция INCOME `shop_withdraw`; `shop_withdraw` добавлен в `GameRules.KNOWN_INCOME_SOURCES`). `HireScreen` переработан: слева визуал стол/стул/работник, справа кнопки «Нанять»/«Уволить»/«Забрать деньги» + учёт магазина (📦 товар, 💰 касса). **Фикс бага AutoCashierAnimation (в хвосте строки).** Ключ спавнера был `LaunchedEffect(buildingId, stock)` — каждая пассивная продажа (каждые 5 игр.мин) меняла `stock` и ПЕРЕЗАПУСКАЛА эффект, обнуляя счётчик `last`, из-за чего боты-покупатели НЕ заходили в сцену. Теперь ключ `LaunchedEffect(buildingId)`, запас читается через `rememberUpdatedState`, боты спавнятся раз в 5 игр.мин. Композиция анимации — только внутри интерьеров лавки/стройки/СТО при нанятом сотруднике (сцена активна = анимация видна). **Фикс бага PassiveSales:** граница продажи теперь «ближайший кратный 5 момент СТРОГО ПОСЛЕ fromMinute» (`((from/step)+1)*step`) — раньше при живом сдвиге минуты на +1 (1 сек) продажа никогда не срабатывала, деньги и сток не менялись. Добегает пропущенный доход после закрытия/перезапуска приложения (минута реконструируется из elapsed, счётчик `UserPrefs.lastPassiveMinute`, DataStore key `last_passive_minute`), новый день (минута побежала назад) сбрасывает продажи. New: `ui/screens/building/AutoCashierAnimation.kt`, `game/PassiveSales.kt`, `UserPrefs.hiredBuildings/setBuildingHired/lastPassiveMinute`, `repo.observeBuildingById/withdrawBuildingCash`, `report/ReportScreen` строка «Зарплата сотрудников: −$salary ₡».

- [x] **Сцена ВНЕШНИЙ ВИД (редактирование персонажа):** На ДОМ-сцене компанованный экран (окно уменьшено до 76dp, коврик 112×38, зверёк 56dp, кнопки 40dp, статы/тексты меньше — всё видно в landscape). Добавлена кнопка «🎨 Внешний вид» (третья в ряду: Корм/Лечение/Внешний вид) → маршрут APPEARANCE + AppearanceScreen (`ui/screens/appearance/AppearanceScreen.kt`): слева живой предпросмотр (фон-подушка + тело + аксессуар на коврике + подпись «Рыжик · Шарф · Рынок»), справа селекторы «Тело/Аксессуар/Фон» (боксы 40dp), кнопка «💾 Сохранить внешний вид» → repo.upsertPet(pet.copy(bodyType/accessory/background)) + popBackStack. Значения подхватываются из PetEntity при открытии (LaunchedEffect). Позже все элементы уменьшены на ~30% (превью 130×160→90×112, боксы селекторов 40→28dp, кнопка сохранения 44→30dp, шрифты пропорционально) — всё влезает в landscape без скролла. Routes.APPEARANCE + composable в NavGraph + onOpenAppearance у MainScreen. Сборка и тесты зелёные.

### Итерации Блока А (конкурс, тз.txt): фазы 1–7 по утверждённому плану
- [x] **Ф1 — Навигация:** NavSidebar — 8 плиток (🏠 ДОМ, 💰 ПЛАН, 🗺️ КАРТА, 🧸 ИГРЫ, 🎯 ЦЕЛИ, 📈 ОТЧЁТ, 📖 СПРАВКА, 👨👩👧 ВЗРОСЛЫЙ; ЖУРНАЛ и ЧИТЫ из сайдбара убраны). Зарегистрирован `Routes.JOURNAL`; HIDDEN_BAR_ROUTES += `"${Routes.CLEANING}/{buildingId}"`; из MainScreen удалены мёртвые колбэки, мёртвый showMenu и лишние импорты. Сборка зелёная.
- [x] **Ф2 — Главный экран:** полоса экономики под статами: `RowScope.InfoChip` — 💼 мешок (cash), 🐷 копилка (savePlan), 🎯 цель (активная, current/target), 📋 задание (первое невыполненное; «всё сделано ✓»). Добавлены BorderStroke/weight-импорты.
- [x] **Ф3 — План-факт (самоконтроль):** `PeriodEntity` (day PK, need/want/save Plan+Fact, income, expense, success; computed balance) + `PeriodDao` (observeAll/last/upsert/countSuccessful). DB v7 + MIGRATION_6_7. `GameRules.daySuccess(needPlan,fact,totalPlan)` — успешный день: факт ≤ план по каждой банке и savePlan>0. Репозиторий: `@Volatile day`, `syncDay(day)`, `insertTx(kind,category,amount,buildingId)` — ВСЕ транзакции (~25 сайтов) теперь датированы игровым днём; `closePeriod()` — снимок в конце дня: plan = остаток+факт, success по daySuccess. `MainViewModel.sleep`/`ReportViewModel.nextDay` вызывают closePeriod ДО `prefs.setCurrentDay`, затем `GameClock.newDay()`+`syncDay`+`payCashierSalary`+`resetDay()` (защита от гонки с `LaunchedEffect(day){ repo.syncDay(day) }` в NavGraph). ReportScreen переписан: план/факт, статус дня ✅/⚠️, история дней, кнопка «Следующий день». Тесты daySuccess.
- [x] **Ф4 — Цели (честный баланс + активная цель):** DB v8 + MIGRATION_7_8: `GoalEntity.isActive`, `GoalDao.clearActive()/active()`. `repo.setActiveGoal(id)` (ровно одна активная), `moveBankToBag`, `withdrawFromGoal` → возврат в **savePlan** (не в cash), `addToGoal` обновляет completed. ensureSeed: первая цель isActive=true. GoalScreen переписан: копилка (сумма, ставка % с учётом игрушек) с диалогами «Пополнить»/«Снять» (возврат в savePlan, не мешок), GoalCard с ⭐ выбором активной, «+50»/«+100», «Снять…» с предпросмотром «ели так — N дней до цели», GoalAmountDialog, PiggyDialog (пресеты 20/50/100). GoalViewModel: goals/wallet/periods/avgDailySave. MainViewModel.currentGoal предпочитает isActive. QuestsScreen показывает `quest.description`.
- [x] **Ф5 — Рост (стадии из ТЗ):** DB v9 + MIGRATION_8_9: PetEntity + successfulDays/mandatoryDays/totalSaved. `GameRules.growthLevel(totalSaved, successfulDays, mandatoryDays)` (стадия 2: сумма ≥200 и обяз. дней ≥3 и успешных ≥2; стадия 3: ≥800 и ≥6 и ≥4), `GameRules.isMandatoryCategory = pet_food/pet_heal`. `TransactionDao.categoriesOfDay(day)`. resetDay считает счётчики из последнего периода, `totalSaved = savePlan + Σ goals.current`, повышает level; `repo.petLevelOnce()`. MainViewModel.sleep — сообщение роста «🎉 Финни вырос!». Тесты роста.
- [x] **Ф6 — Сброс + Демо-режим:** `UserPrefs.demoMode` + `clear()` (DataStore). `GameRules/Balance.DEMO_DAILY_ALLOWANCE = 200`. `GameClock.setDemoMode(on)` + `@Volatile demoMode` → delay 200мс (вместо 1с) = часы в 5× быстрее; тик считывается при start из prefs. `repo.resetProfile()` = `db.clearAllTables()` → `ensureSeed()`. `repo.demoDailyAllowance()` → мешок + INCOME daily_allowance (MainViewModel.sleep при demoMode). Свободный сон в демо: sleep не проверяет GameRules.canSleep при demoMode. AdultScreen переписан: арифметический барьер (12−5) → «Профиль ребёнка» (питомец/день/мешок/копилка/цель/задания/успешные дни/счётчики роста), тумблер «🧪 Демо-режим» (prefs+GameClock), кнопка «🔄 Сбросить профиль» (vm.resetProfile). AdultViewModel создан.
- [x] **Ф7 — Портрет:** из AndroidManifest убран `screenOrientation="sensorLandscape"` — приложение теперь поворачивается свободно. TimeBankBar: статусная строка укорочена («🟢 Рабочий день»/«🌙 Вечер — можно спать»/«😴 Пора спать»), maxLines=1, отступы банков 6dp — вписывается в узкий портрет. **Адаптивная панель навигации:** корень NavGraph — `BoxWithConstraints`, ландшафт = `Row` (сайдбар слева, вертикальные плитки со скроллом), портрет = `Column` (контент + панель ВНИЗУ экрана, `NavSidebar(bottomBar=true)` — горизонтальный `Row` 52dp с `horizontalScroll`). Ориентация для баннера сна — `LocalConfiguration` (padding снизу 66dp в портрете, чтобы не наезжать на панель). MapScreen — LazyVerticalGrid(3 колонки) сам скроллится. Интерьеры/мини-игры — гибкие веса + скроллы (карта/интерьеры = скролл, ландшафтный канвас не масштабируется — см. известные ограничения).
- [x] **Правки навигации (по ревью):** ЧИТЫ перенесены ВО ВЗРОСЛЫЙ: AdultScreen = арифметический барьер (12−5) → профиль ребёнка + демо-тумблер + сброс + встроенные ЧИТЫ (`CheatsScreen(embedded=true)`, «🎁 Читы — для теста» — заголовок только в отдельном режиме). Из сайдбара убран пункт «🎁 ЧИТЫ» (+ из NavGraph удалён `composable(Routes.CHEATS)`, повтор обучения — колбэк в ADULT). Кнопки «← Назад» убраны: ИГРЫ (EntertainmentScreen), СПРАВКА (ReferenceScreen), навигация только сайдбаром. ЦЕЛИ (GoalHubScreen): убраны шапка (название+«◀») и нижняя кнопка — остались только вкладки Цели/Задания/Прогресс, поднятые вверх. **Фикс «читы не видалны»:** в portrait/landscape верхняя профильная секция (verticalScroll без веса) съедала всю высоту и `Box(weight(1f))` с читами получал 0dp → ЧИТЫ не отображались. Теперь `AdultScreen` — одна общая `Column(verticalScroll)`, а `CheatsScreen(embedded=true)` НЕ создаёт собственный скролл/фон (`if (embedded) Modifier.fillMaxWidth()` вместо fillMaxSize+scroll) и скроллится вместе с экраном. Сборка зелёная.
- [x] **Правки UI (портрет):** Интерьеры зданий: кнопки «🧾 Касса» + «👤 Сотрудник/Нанять» вынесены в общий `ShopActionButtons(vertical, hired, ...)` (`ui/screens/building/ShopActionButtons.kt`) — в портрете кнопки в столбик (касса выше, сотрудник под ней), в ландшафте рядом (как раньше). ДОМ: полоса чипов сокращена до одного блока цели (💼/🐷/📋 убраны — банки видны в HUD), текст блока «🎯 Цель: {название} {сумма}/{цель}»; кнопки «Корм/Лечение/Внешний вид» в портрете — в столбик (иначе «Внешний вид» обрезался), в ландшафте — ряд. Ориентация — `LocalConfiguration.screenWidthDp < screenHeightDp`. Сборка и тесты зелёные.

### Этап 1. Неделя 1 — Ядро мира P0 (Карта + Финни + одно здание Продукты)
- [x] **День 1:** Top-down карта на Compose (один район «Рынок»): дороги, живое здание «Продукты», живой дом. MapScreen (моки) + HUD + маршрут MAP. (Навигация из плитки «Мир».)
- [x] **День 2:** Перемещение Финни — виртуальный джойстик (левый) + тап-по-точке. Вход в здание тапом по зданию → BUILDING/{id}. Коллизии пока упрощены.
- [x] **День 3:** Сцена внутри здания: BuildingInteriorScreen (горячие точки: закупка, полки, ценник, касса, найм, улучшения, учёт), работает с BuildingEntity из БД (stock/price/costPrice/soldToday). Под-экраны подключены маршрутами SUPPLIERS/PRICER/HIRE/CASHIER.
- [x] **День 4:** Боты-NPC на карте: 5 ботов из БД ходят (Animatable) дом↔март, заходят в здание, работают, покупают, выходят. Визуально: спрайт по типу (🚶/🚗/🕴️/…), над головой статус (🛠️/🛍️/🏠). Позиция/состояние пишутся в БД через repo.updateBotStatePosition.
- [x] **День 5:** Жизненный цикл бота в здании: визуальный цикл на карте + экономика simulateDay() (зарплата→траты→выручка). Боты привязаны к зданию (workBuildingId); в здании кнопка «🚀 Запустить день» с итогом (ЗП/выручка/себест./аренда/налог/прибыль).
- [x] **День 6:** Зарплата: в BuildingInteriorScreen карточка «👩‍💼 Сотрудники и зарплата» с кнопкой «ЗП N ₡» на бота (списывается из Wallet → wallet бота → бот может тратить). В конце дня зарплаты идут автоматически в simulateDay().
- [x] **День 7:** Траты ботов: BuildingCashierScreen — боты (покупатели из BotBrain.estimateBuyers) идут дверь→полка (stock-- через takeItemFromBuilding)→касса; игрок пробивает (CashierScene: тащит товар, выдаёт сдачу), completeSaleInBuilding пишет выручку/транзакции; при выходе необслуженный товар возвращается (refundStockToBuilding). UI: без скролла — шапка (◀ + «🧾 Живая касса здания» + «💰 N ₡») перенесена внутрь левого блока над комнатой, оба блока начинаются от верха экрана; правый прилавок компактный (покупатель, доска 156dp, сдача в 5 вариантов × 2 ряда, вердикт). Фикс DND: летящий 🧃 теперь прямой ребёнок доски (координаты доски, а не ряда корзины), иначе спрайт отрисовывался ниже пальца на высоту ряда.

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

- [x] **Сцена НАВИГАЦИЯ ЧЕРЕЗ КАРТУ (только ларёк):** Из левого сайдбара (NavSidebar) убраны кнопки «🛒 ЛАРЁК», «🔧 СТРОЙКА», «🚗 СТО» — игрок заходит в магазины только через карту (MapScreen → тап по зданию → BUILDING/{id}). Карта района «Рынок» (MapScreen.cityMap) перестроена: в верхнем ряду слева — ДОМ (h1), справа от него — единственный стартовый ларёк (b2, complex с мини-сеткой, buildingType=PRODUCTS), остальные участки верхнего ряда и района — зелёные «продаётся» (s1..s18, LotScreen предлагает Ларёк/Стройку/СТО). ensureSeed() больше НЕ создаёт AUTO_SERVICE и CONSTRUCTION (только «Продукты»; миграция CONSTRUCTION удалена; бот-механик ENGINEER workBuildingId=-1 встанет, когда игрок построит СТО на участке). Навигационные route'ы КIOSK/CONSTRUCTION/AUTO_SERVICE в NavGraph и TimeBankBar оставлены (недостижимы из сайдбара, но валидны для tutorial/отладки). TutorialScenarios: из «Меню слева» убраны ларёк/стройка/СТО, шаги СТРОЙКА/СТО удалены, шаги ЛАРЁК остались (KIOSK). Сборка и тесты зелёные.
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
- `UserPrefs` — добавлено `gameMinute`, `gameClockEpoch` (персист часов), `setGameClock(minute, epochMs)`, `demoMode` (Flow+setter, тик часов ×5 в демо), `clear()` (полный сброс DataStore), `setTutorialDone`, `setDemoMode`.
- `PetEntity(id=1, name, bodyType:0..2, accessory:0..2, background:0, hunger 0..100, mood, energy, level:1..3, xp (опыт развлечений), successfulDays, mandatoryDays, totalSaved, x, y)` — позиция на карте
- `PeriodEntity(day PK, need/want/save Plan+Fact, income, expense, success)` + `PeriodDao` (observeAll/last/upsert/countSuccessful) — план-факт дня (Ф3).
- `WalletEntity(id=1, cash, needPlan, wantPlan, savePlan, needFact, wantFact, saveFact)`
- `BuildingEntity(id, type:PRODUCTS/AUTO_SERVICE/CONSTRUCTION/HEALTH/ART/RESIDENTIAL, district, x, y, level, stock, price, costPrice, cash, soldToday, revenueTotal, dirtLevel, upgradesBitmask, employeesJson, isOpen, plotId)`
- `BotEntity(id, type:WORKER/ENGINEER/MANAGER/RETIREE/STUDENT/ELITE, homeBuildingId, workBuildingId, salary, wallet, hasCar, loyalty, state:HOME/WORKING/SHOPPING/MOVING, targetX, targetY)`
- `TransactionEntity(day, kind, category, amount, source, buildingId)`
- `GoalEntity(id, title, targetAmount, currentAmount, completed, isActive)`
- `QuestEntity(id, topic, title, description, reward, completed, day, order, progress, target)` — topic: PLANNING/SAVING/SPENDING/PLAY
- `CatalogItemEntity` added: `description`, `xpReward` (игрушки WANT дают опыт развлечений)
- `UserPrefs` — `petName`, `nickname`, `bodyType`, `accessory`, `background`, `soundEnabled`, `difficulty`, `onboardingDone`, `currentDay`, `demoMode`, `tutorialDone`, `hiredBuildings:Set<Long>`, `growthStage`, `currentDistrict`

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

---

## 7. Сцены и маршруты (актуальный список)

См. `ui/navigation/Routes.kt` – все маршруты.

| Route | Сцена | Путь в `ui/screens/` |
|---|---|---|
| `main` | ДОМ: Финни, корм/лечение/внешний вид, сон | `main/MainScreen.kt` |
| `plan` | ПЛАН: мешок → 3 банки (drag-and-drop монет) | `banks/BankScreen.kt` |
| `map` | КАРТА: район «Рынок», участки, магазины, боты | `/map/MapScreen.kt` |
| `games` | ИГРЫ: уровень развлечений, каталог игрушек WANT, PLAY-задания | `entertainment/EntertainmentScreen.kt` |
| `appearance` | ВНЕШНИЙ ВИД: тело/аксессуар/фон Финни | `appearance/AppearanceScreen.kt` |
| `progress` | Прогресс / квесты (PLANNING/SAVING/SPENDING/PLAY) | `quests/ProgressScreen.kt` |
| `reference` | Справка | `reference/ReferenceScreen.kt` |
| `adult` | ВЗРОСЛЫЙ: барьер (12−5) → профиль + демо + сброс + ЧИТЫ (встроены) | `adult/AdultScreen.kt` |
| `goals` | ЦЕЛИ: вкладки Цели/Задания/Прогресс (без шапки и «назад») | `goals/GoalHubScreen.kt` |
| `report` | ОТЧЁТ дня (план/факт; новый день — только через сон на ДОМ) | `report/ReportScreen.kt` |
| `kiosk` | ЛАРЁК (интерьер здания «Продукты») | `kiosk/KioskScreen.kt` |
| `building/{id}` | Интерьер здания (Продукты/Стройка/СТО по типу) | `building/BuildingInteriorScreen.kt` и `Products/Construction/AutoServiceInteriorScreen.kt` |
| `suppliers/{buildingId}` | Закупка товара у поставщиков | `game/SuppliersGame.kt` |
| `pricer/{buildingId}` | Ценник (слайдер цены + прогноз) | `game/PricerGame.kt` |
| `shelves/{buildingId}` | Расстановка товара на полки | `game/ShelvesGame.kt` |
| `hire/{buildingId}` | Найм сотрудника / забрать выручку здания | `building/HireScreen.kt` |
| `cashier/{buildingId}` | Живая касса здания | `building/BuildingCashierScreen.kt` (внутри `game/CashierScene.kt`) |
| `cleaning` | Уборка (tap-to-clean 4×4) | `game/CleaningGameScreen.kt` |
| `lots/{plotId}` | Покупка участка / выбор типа здания | `map/LotScreen.kt` |
| `journal` | Журнал транзакций (встроен в ОТЧЁТ) | `journal/JournalScreen.kt` |
| `onboarding` | Онбординг (выбор питомца, имя) | `onboarding/OnboardingScreen.kt` |
| `kiosk`/`construction`/`auto_service` | legacy-маршруты, из сайдбара убраны (доступны через карту/отладку) | — |

**Горячие точки интерьера здания:** закупка, полки, ценник, касса (живая), найм, уборка, улучшения, учёт, запуск дня.

**Фоновые сервисы:** `GameClock` (часы), `PassiveSales` (авто-касса нанятых работников каждые 5 игр.мин), `PetDecayWorker` (1ч, потребности питомца).