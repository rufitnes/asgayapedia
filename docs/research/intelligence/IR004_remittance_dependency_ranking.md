# IR004_sources_remittance_data.md – Sources for Remittance Dependency Analysis

**Type:** Intelligence Report  
**Date:** March 24, 2026  
**Related:** IR004_remittance_dependency_ranking.md  

---

## 1. Banco de España – Remittance Data

**Title:** Las remesas enviadas desde España hacia América Latina: algunas cifras básicas  
**Author(s):** Juan Carlos Berganza, María Pla Cobián González, María Teresa García Cid, Esther López Espinosa  
**Publication:** Boletín Económico – Banco de España, 2025/T2, Artículo 01  
**DOI:** https://doi.org/10.53479/39338  
**URL:** [https://www.bde.es/f/webbe/SES/Secciones/Publicaciones/InformesBoletinesRevistas/BoletinEconomico/25/T2/Fich/be2502-art01.pdf](https://www.bde.es/f/webbe/SES/Secciones/Publicaciones/InformesBoletinesRevistas/BoletinEconomico/25/T2/Fich/be2502-art01.pdf)  

**Key tables used:**
- Table 2 – Distribution by destination country of remittances sent from Spain (2013–2023)
- Table 1 – Distribution by geographic area

**Local copy:** Stored in `knowledge/research/` (original PDF) and referenced in research sessions RS008 and RS009.

---

## 2. SEDLAC – Household Income Data

**Source:** Socio-Economic Database for Latin America and the Caribbean (CEDLAS and The World Bank)  
**Version:** September 2025  
**Files used:**
- `2025_Act1_surveys_LAC.xlsx` – Metadata on household surveys
- `2025_Act1_incomes_LAC.xlsx` – Per capita income by deciles (national, urban, rural), in local currency units

**Local storage:**
- Both files are located in `knowledge/intelligence/` folder.

**Access:**
- SEDLAC public portal: [https://www.cedlas.econo.unlp.edu.ar/wp/estadisticas/sedlac/](https://www.cedlas.econo.unlp.edu.ar/wp/estadisticas/sedlac/)
- World Bank LAC Equity Lab: [https://www.worldbank.org/en/topic/poverty/lac-equity-lab1/resources](https://www.worldbank.org/en/topic/poverty/lac-equity-lab1/resources)

**Key data extracted:**
- Mean per capita income (national) for 2023/2024 for each country, from sheet `deciles_pci`.

---

## 3. Exchange Rates (Approximate, 2023)

| Currency | Rate (1 EUR) | Source |
|----------|--------------|--------|
| Honduran Lempira | 27.5 | OANDA / Central Bank of Honduras |
| Paraguayan Guaraní | 6,500 | OANDA / Central Bank of Paraguay |
| Bolivian Boliviano | 7.5 | OANDA / Central Bank of Bolivia |
| US Dollar (Ecuador) | 1.08 | European Central Bank |
| Dominican Peso | 58 | OANDA / Central Bank of Dominican Republic |
| Colombian Peso | 4,600 | OANDA / Central Bank of Colombia |
| Peruvian Sol | 4.0 | OANDA / Central Bank of Peru |

*These rates are indicative; for a refined analysis, official annual averages should be used.*

---

## 4. Additional Notes

- The SEDLAC files also contain data on **income from remittances** and **share of households receiving remittances**. This information is available in the income composition tables (not extracted here) and would allow a more precise calculation of remittance dependency at the household level.
- The Banco de España report provides remittance totals by country; population figures used for per‑capita calculations are from World Bank (2023).

---

*Prepared by DeepSeek, Main Research Assistant, March 24, 2026*
