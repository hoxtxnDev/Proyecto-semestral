import { useMemo, useState } from "react";

type Role = "Técnico" | "Jefatura" | "Finanzas";
type IconName =
  | "alert"
  | "arrow"
  | "calendar"
  | "camera"
  | "chart"
  | "check"
  | "chevron"
  | "clock"
  | "download"
  | "eye"
  | "filter"
  | "fuel"
  | "home"
  | "hotel"
  | "location"
  | "meal"
  | "parking"
  | "receipt"
  | "search"
  | "settings"
  | "wallet"
  | "x";

const iconPaths: Record<IconName, React.ReactNode> = {
  alert: <><circle cx="12" cy="12" r="9" /><path d="M12 7.5v5M12 16h.01" /></>,
  arrow: <><path d="m5 12 14 0M14 7l5 5-5 5" /></>,
  calendar: <><rect x="3.5" y="5" width="17" height="15" rx="3" /><path d="M8 3v4M16 3v4M3.5 10h17" /></>,
  camera: <><path d="M4 8.5h3l1.5-2h7l1.5 2h3v10H4z" /><circle cx="12" cy="13.5" r="3.2" /></>,
  chart: <><path d="M5 19V9M12 19V5M19 19v-7" /><path d="M3 19h18" /></>,
  check: <path d="m5 12.5 4.3 4.3L19 7.2" />,
  chevron: <path d="m9 7 5 5-5 5" />,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3.2 2" /></>,
  download: <><path d="M12 3v12M7.5 11.5 12 16l4.5-4.5" /><path d="M4 20h16" /></>,
  eye: <><path d="M2.5 12s3.2-5.5 9.5-5.5 9.5 5.5 9.5 5.5-3.2 5.5-9.5 5.5S2.5 12 2.5 12Z" /><circle cx="12" cy="12" r="2.5" /></>,
  filter: <path d="M4 6h16M7 12h10M10 18h4" />,
  fuel: <><path d="M5 20V5h9v15M4 20h11M8 8h3" /><path d="m14 8 3 3v6.5a1.5 1.5 0 0 0 3 0V9l-2-2" /></>,
  home: <><path d="m3 11 9-7 9 7" /><path d="M5.5 9.5V20h13V9.5M9.5 20v-6h5v6" /></>,
  hotel: <><path d="M4 20V5h12v15M8 9h.01M12 9h.01M8 13h.01M12 13h.01M16 11h4v9M2 20h20" /></>,
  location: <><path d="M19 10c0 5.2-7 11-7 11S5 15.2 5 10a7 7 0 1 1 14 0Z" /><circle cx="12" cy="10" r="2.3" /></>,
  meal: <><path d="M7 3v7M4 3v4.5A2.5 2.5 0 0 0 6.5 10H7M7 10v11M16 3v18M16 3c3 2.5 4 6 0 9" /></>,
  parking: <><circle cx="12" cy="12" r="9" /><path d="M10 17V7h3a3 3 0 0 1 0 6h-3" /></>,
  receipt: <><path d="M6 3h12v18l-3-2-3 2-3-2-3 2z" /><path d="M9 8h6M9 12h6M9 16h3" /></>,
  search: <><circle cx="10.5" cy="10.5" r="6.5" /><path d="m15.5 15.5 4.5 4.5" /></>,
  settings: <><circle cx="12" cy="12" r="3" /><path d="M19 13.5v-3l-2-.7-.5-1.2.9-1.9-2.1-2.1-1.9.9-1.2-.5-.7-2h-3l-.7 2-1.2.5-1.9-.9-2.1 2.1.9 1.9-.5 1.2-2 .7v3l2 .7.5 1.2-.9 1.9 2.1 2.1 1.9-.9 1.2.5.7 2h3l.7-2 1.2-.5 1.9.9 2.1-2.1-.9-1.9.5-1.2z" /></>,
  wallet: <><path d="M4 6.5h14a2 2 0 0 1 2 2v10H4a2 2 0 0 1-2-2v-12a2 2 0 0 1 2-2h12v4" /><path d="M15 11h7v5h-7a2.5 2.5 0 0 1 0-5Z" /></>,
  x: <path d="m6 6 12 12M18 6 6 18" />,
};

function Icon({ name, size = 20 }: { name: IconName; size?: number }) {
  return (
    <svg
      aria-hidden="true"
      className="icon"
      fill="none"
      height={size}
      viewBox="0 0 24 24"
      width={size}
    >
      <g stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8">
        {iconPaths[name]}
      </g>
    </svg>
  );
}

function Brand({ inverted = false }: { inverted?: boolean }) {
  return (
    <div className={inverted ? "brand brand-inverted" : "brand"}>
      <span className="brand-mark"><span /></span>
      <span className="brand-name">Teq<span>Gastos</span></span>
    </div>
  );
}

function Field({
  label,
  placeholder,
  value,
  onChange,
  status,
  type = "text",
  suffix,
}: {
  label: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  status?: "valid" | "error";
  type?: string;
  suffix?: React.ReactNode;
}) {
  return (
    <label className={`field ${status ? `field-${status}` : ""}`}>
      <span className="field-label">{label}</span>
      <span className="field-control">
        <input
          onChange={(event) => onChange(event.target.value)}
          placeholder={placeholder}
          type={type}
          value={value}
        />
        {suffix ?? (status && <span className="field-status"><Icon name={status === "valid" ? "check" : "alert"} size={17} /></span>)}
      </span>
    </label>
  );
}

function BottomNav({ active }: { active: string }) {
  const items: { label: string; icon: IconName }[] = [
    { label: "Inicio", icon: "home" },
    { label: "Rendiciones", icon: "receipt" },
    { label: "Reportes", icon: "chart" },
    { label: "Ajustes", icon: "settings" },
  ];
  return (
    <nav className="bottom-nav" aria-label="Navegación principal">
      {items.map((item) => (
        <button className={active === item.label ? "nav-item active" : "nav-item"} key={item.label}>
          <Icon name={item.icon} size={21} />
          <span>{item.label}</span>
        </button>
      ))}
    </nav>
  );
}

function AppHeader({
  eyebrow,
  title,
  subtitle,
  onBack,
}: {
  eyebrow: string;
  title: string;
  subtitle?: string;
  onBack: () => void;
}) {
  return (
    <header className="app-header">
      <div className="header-top">
        <Brand inverted />
        <button className="avatar-button" onClick={onBack} aria-label="Cerrar sesión">TS</button>
      </div>
      <p className="header-eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      {subtitle && <p className="header-subtitle">{subtitle}</p>}
      <div className="search-box">
        <Icon name="search" size={18} />
        <input aria-label="Buscar" placeholder="Buscar rendiciones..." />
        <button aria-label="Filtrar"><Icon name="filter" size={18} /></button>
      </div>
    </header>
  );
}

function LoginScreen({ onLogin }: { onLogin: (role: Role) => void }) {
  const [role, setRole] = useState<Role>("Técnico");
  const [email, setEmail] = useState("12.345.678-9");
  const [password, setPassword] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const emailValid = email.length >= 9;
  const passwordValid = password.length >= 4;

  return (
    <div className="login-screen">
      <div className="login-hero">
        <div className="login-brand-row">
          <Brand inverted />
          <span className="company-tag">TEQMED SPA</span>
        </div>
        <div className="hero-orbit orbit-one" />
        <div className="hero-orbit orbit-two" />
        <div className="hero-copy">
          <span className="secure-pill"><Icon name="check" size={13} /> ACCESO SEGURO</span>
          <h1>Gastos simples.<br /><span>Control total.</span></h1>
          <p>Rinde, aprueba y procesa gastos desde un solo lugar.</p>
        </div>
      </div>

      <main className="login-panel">
        <div className="section-heading">
          <div>
            <p className="overline">BIENVENIDO</p>
            <h2>Inicia sesión</h2>
          </div>
          <div className="step-number">01</div>
        </div>

        <div className="profile-block">
          <p className="mini-label">PERFIL DE PRUEBA</p>
          <div className="role-selector">
            {(["Técnico", "Jefatura", "Finanzas"] as Role[]).map((item) => (
              <button
                className={role === item ? "role-chip selected" : "role-chip"}
                key={item}
                onClick={() => setRole(item)}
              >
                {item}
              </button>
            ))}
          </div>
        </div>

        <div className="login-form">
          <Field
            label="Email o RUT"
            onChange={setEmail}
            placeholder="nombre@teqmed.cl"
            status={submitted ? (emailValid ? "valid" : "error") : undefined}
            value={email}
          />
          <Field
            label="Contraseña"
            onChange={setPassword}
            placeholder="Ingresa tu contraseña"
            status={submitted ? (passwordValid ? "valid" : "error") : undefined}
            suffix={<Icon name="eye" size={18} />}
            type="password"
            value={password}
          />
          {submitted && !passwordValid && (
            <p className="form-error"><Icon name="alert" size={14} /> Ingresa al menos 4 caracteres.</p>
          )}
          <button
            className="primary-button"
            onClick={() => {
              setSubmitted(true);
              if (emailValid && passwordValid) onLogin(role);
            }}
          >
            Ingresar como {role}
            <span><Icon name="arrow" size={19} /></span>
          </button>
        </div>
        <p className="login-footer">Plataforma de uso interno · Teqmed SPA</p>
      </main>
    </div>
  );
}

const categories: { name: string; cap: string; icon: IconName }[] = [
  { name: "Alimentación", cap: "$15.000/día", icon: "meal" },
  { name: "Alojamiento", cap: "$65.000/noche", icon: "hotel" },
  { name: "Combustible", cap: "$60.000/carga", icon: "fuel" },
  { name: "Peaje / Estac.", cap: "$25.000/evento", icon: "parking" },
  { name: "Insumos", cap: "$50.000/compra", icon: "receipt" },
];

function TechnicianScreen({ onLogout }: { onLogout: () => void }) {
  const [category, setCategory] = useState(categories[0]);
  const [photo, setPhoto] = useState(false);
  const [rut, setRut] = useState("");
  const [folio, setFolio] = useState("");
  const [amount, setAmount] = useState("");

  return (
    <div className="app-screen">
      <AppHeader eyebrow="NUEVA RENDICIÓN" title="Registrar gasto" subtitle="Completa los datos de tu comprobante." onBack={onLogout} />
      <main className="screen-content">
        <button className="cost-center-card">
          <span className="cost-icon"><Icon name="wallet" /></span>
          <span><small>CENTRO DE COSTO ACTIVO</small><strong>CC-104 · Operaciones Norte</strong></span>
          <Icon name="chevron" size={19} />
        </button>

        <section className="form-card">
          <div className="card-title-row">
            <div><p className="overline">COMPROBANTE</p><h2>Datos del gasto</h2></div>
            <span className="step-badge">1 DE 2</span>
          </div>
          <div className="two-column">
            <Field label="RUT Proveedor" onChange={setRut} placeholder="76.123.456-7" status={rut.length > 8 ? "valid" : undefined} value={rut} />
            <Field label="Folio" onChange={setFolio} placeholder="Ej. 008321" value={folio} />
          </div>
          <Field
            label="Monto"
            onChange={setAmount}
            placeholder="0"
            suffix={<span className="currency-suffix">CLP</span>}
            type="number"
            value={amount}
          />

          <div className="category-section">
            <p className="field-label">Categoría</p>
            <div className="category-grid">
              {categories.map((item) => (
                <button
                  className={category.name === item.name ? "category-item selected" : "category-item"}
                  key={item.name}
                  onClick={() => setCategory(item)}
                >
                  <span className="category-icon"><Icon name={item.icon} size={21} /></span>
                  <strong>{item.name}</strong>
                  <small>{item.cap}</small>
                </button>
              ))}
            </div>
          </div>
        </section>

        <section className="rates-card">
          <div className="rates-heading"><span><Icon name="chart" size={18} /></span><div><strong>Conversión offline</strong><small>Tasas estáticas disponibles</small></div></div>
          <div className="rate-values">
            <div><small>1 UF</small><strong>$38.000 <em>CLP</em></strong></div>
            <span />
            <div><small>1 USD</small><strong>$950 <em>CLP</em></strong></div>
          </div>
        </section>

        <section className="capture-card">
          <div className="card-title-row">
            <div><p className="overline">RESPALDO</p><h2>Comprobante</h2></div>
            {photo && <span className="success-badge"><Icon name="check" size={13} /> LISTO</span>}
          </div>
          <div className={photo ? "receipt-capture captured" : "receipt-capture"}>
            {photo ? (
              <div className="receipt-preview">
                <span>BOLETA ELECTRÓNICA</span><i /><i /><i /><b>TOTAL&nbsp;&nbsp; $24.990</b>
              </div>
            ) : <span className="camera-icon"><Icon name="camera" size={28} /></span>}
            <div>
              <strong>{photo ? "Comprobante capturado" : "Fotografía tu boleta"}</strong>
              <small>{photo ? "IMG_3841.jpg · 1,2 MB" : "Asegúrate de que sea legible"}</small>
            </div>
            <button onClick={() => setPhoto(!photo)}>{photo ? "Cambiar" : "Tomar foto"}</button>
          </div>
          <div className="location-badge">
            <span><Icon name="location" size={18} /></span>
            <div><small>UBICACIÓN CAPTURADA</small><strong>-33.4489, -70.6693</strong></div>
            <Icon name="check" size={16} />
          </div>
        </section>

        <button className="primary-button submit-expense">Enviar rendición <span><Icon name="arrow" size={19} /></span></button>
      </main>
      <BottomNav active="Rendiciones" />
    </div>
  );
}

const expenses = [
  { name: "Camila Torres", rut: "17.982.341-2", folio: "F-008321", category: "Alimentación", amount: 24990, icon: "meal" as IconName },
  { name: "Diego Soto", rut: "16.440.828-6", folio: "B-114209", category: "Combustible", amount: 58700, icon: "fuel" as IconName },
  { name: "Martín Rojas", rut: "18.223.901-4", folio: "F-003118", category: "Alojamiento", amount: 65000, icon: "hotel" as IconName },
];

function ManagerScreen({ onLogout }: { onLogout: () => void }) {
  const [items, setItems] = useState(expenses);
  const [modal, setModal] = useState<{ expense: typeof expenses[number]; action: "Observar" | "Rechazar" } | null>(null);
  const [comment, setComment] = useState("");

  const resolve = (expense: typeof expenses[number]) => setItems((current) => current.filter((item) => item.folio !== expense.folio));

  return (
    <div className="app-screen">
      <AppHeader eyebrow="JEFATURA" title="Por evaluar" subtitle="Revisa los gastos enviados por tu equipo." onBack={onLogout} />
      <main className="screen-content">
        <section className="summary-card">
          <div><p>PENDIENTES</p><strong>{String(items.length).padStart(2, "0")}</strong><small>rendiciones</small></div>
          <div className="summary-total"><small>MONTO TOTAL</small><strong>${items.reduce((sum, item) => sum + item.amount, 0).toLocaleString("es-CL")}</strong><span>CLP</span></div>
          <span className="summary-icon"><Icon name="clock" size={24} /></span>
        </section>

        <div className="list-header"><div><p className="overline">BANDEJA DE ENTRADA</p><h2>Rendiciones recientes</h2></div><button><Icon name="filter" size={17} /> Filtrar</button></div>

        <div className="expense-list">
          {items.map((expense) => (
            <article className="expense-card" key={expense.folio}>
              <div className="expense-person">
                <span className="person-avatar">{expense.name.split(" ").map((part) => part[0]).join("").slice(0, 2)}</span>
                <div><strong>{expense.name}</strong><small>{expense.rut}</small></div>
                <span className="pending-pill">PENDIENTE</span>
              </div>
              <div className="expense-body">
                <div className="receipt-thumb"><Icon name="receipt" size={26} /><small>VER</small></div>
                <div className="expense-details">
                  <span className="expense-category"><Icon name={expense.icon} size={15} /> {expense.category}</span>
                  <small>FOLIO {expense.folio}</small>
                  <strong>${expense.amount.toLocaleString("es-CL")} <em>CLP</em></strong>
                </div>
              </div>
              <div className="expense-actions">
                <button className="approve" onClick={() => resolve(expense)}><Icon name="check" size={16} /> Aprobar</button>
                <button className="observe" onClick={() => setModal({ expense, action: "Observar" })}><Icon name="alert" size={16} /> Observar</button>
                <button className="reject" onClick={() => setModal({ expense, action: "Rechazar" })}><Icon name="x" size={16} /> Rechazar</button>
              </div>
            </article>
          ))}
          {items.length === 0 && <div className="empty-state"><span><Icon name="check" size={28} /></span><h2>Todo al día</h2><p>No quedan rendiciones pendientes.</p></div>}
        </div>
      </main>
      <BottomNav active="Inicio" />

      {modal && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setModal(null)}>
          <section className="action-modal" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
            <div className={`modal-icon ${modal.action === "Rechazar" ? "danger" : ""}`}><Icon name={modal.action === "Rechazar" ? "x" : "alert"} size={23} /></div>
            <button className="modal-close" onClick={() => setModal(null)} aria-label="Cerrar"><Icon name="x" size={18} /></button>
            <p className="overline">{modal.action.toUpperCase()} RENDICIÓN</p>
            <h2>Cuéntanos el motivo</h2>
            <p>Tu comentario será enviado a {modal.expense.name} para que pueda revisar este gasto.</p>
            <label className="comment-field">
              <span>Justificación obligatoria</span>
              <textarea maxLength={240} onChange={(event) => setComment(event.target.value)} placeholder="Escribe una explicación clara..." value={comment} />
              <small>{comment.length}/240</small>
            </label>
            <button
              className={modal.action === "Rechazar" ? "modal-submit danger" : "modal-submit"}
              disabled={!comment.trim()}
              onClick={() => { resolve(modal.expense); setModal(null); setComment(""); }}
            >
              Confirmar y enviar
            </button>
          </section>
        </div>
      )}
    </div>
  );
}

function FinanceScreen({ onLogout }: { onLogout: () => void }) {
  const [paymentCode, setPaymentCode] = useState("");
  const approved = useMemo(() => [
    { ...expenses[0], date: "18 Jun 2025" },
    { ...expenses[1], date: "18 Jun 2025" },
    { name: "Paula Díaz", rut: "19.118.220-1", folio: "B-006742", category: "Peaje / Estac.", amount: 18500, icon: "parking" as IconName, date: "17 Jun 2025" },
  ], []);
  const total = approved.reduce((sum, item) => sum + item.amount, 0);

  const exportCsv = () => {
    const rows = ["RUT,Folio,Categoria,Monto,CodigoPago", ...approved.map((item) => `${item.rut},${item.folio},${item.category},${item.amount},${paymentCode}`)];
    const link = document.createElement("a");
    link.href = URL.createObjectURL(new Blob([rows.join("\n")], { type: "text/csv" }));
    link.download = "teqgastos_erp.csv";
    link.click();
    URL.revokeObjectURL(link.href);
  };

  return (
    <div className="app-screen finance-screen">
      <AppHeader eyebrow="FINANZAS" title="Reembolsos" subtitle="Prepara los gastos aprobados para pago." onBack={onLogout} />
      <main className="screen-content">
        <section className="finance-total">
          <div className="finance-total-top"><span><Icon name="wallet" size={21} /></span><p>LISTO PARA DESEMBOLSO</p><small>CLP</small></div>
          <strong><sup>$</sup>{total.toLocaleString("es-CL")}</strong>
          <div className="finance-meta"><span><b>{approved.length}</b> rendiciones aprobadas</span><span><Icon name="check" size={14} /> Validado</span></div>
        </section>

        <div className="list-header finance-list-header">
          <div><p className="overline">APROBADOS</p><h2>Detalle de reembolsos</h2></div>
          <button><Icon name="filter" size={17} /> Filtrar</button>
        </div>
        <div className="approved-list">
          {approved.map((item) => (
            <article className="approved-item" key={item.folio}>
              <span className="approved-icon"><Icon name={item.icon} size={20} /></span>
              <div><strong>{item.name}</strong><small>{item.folio} · {item.date}</small><span>{item.category}</span></div>
              <p>${item.amount.toLocaleString("es-CL")}<small>CLP</small></p>
            </article>
          ))}
        </div>

        <section className="export-card">
          <div className="export-heading"><span><Icon name="download" size={21} /></span><div><p className="overline">INTEGRACIÓN ERP</p><h2>Generar archivo de pago</h2></div></div>
          <Field
            label="Código de Pago Final"
            onChange={setPaymentCode}
            placeholder="Ej. PAG-2025-0041"
            status={paymentCode.length > 5 ? "valid" : undefined}
            value={paymentCode}
          />
          <button className="primary-button export-button" disabled={paymentCode.length <= 5} onClick={exportCsv}>
            Exportar ERP (.CSV)
            <span><Icon name="download" size={19} /></span>
          </button>
          <p className="export-note"><Icon name="check" size={13} /> El archivo incluirá solo rendiciones aprobadas.</p>
        </section>
      </main>
      <BottomNav active="Reportes" />
    </div>
  );
}

export default function App() {
  const [role, setRole] = useState<Role | null>(null);
  if (!role) return <LoginScreen onLogin={setRole} />;
  if (role === "Técnico") return <TechnicianScreen onLogout={() => setRole(null)} />;
  if (role === "Jefatura") return <ManagerScreen onLogout={() => setRole(null)} />;
  return <FinanceScreen onLogout={() => setRole(null)} />;
}
