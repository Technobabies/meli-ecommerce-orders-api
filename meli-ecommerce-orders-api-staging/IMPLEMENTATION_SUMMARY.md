# ✅ IMPLEMENTACIÓN COMPLETADA - Endpoint PUT para Cards

## Resumen Ejecutivo

El endpoint **PUT /api/v1/cards/{id}** ha sido completamente implementado en el backend y está listo para ser consumido desde el frontend.

---

## ¿Qué se implementó?

### 1. Endpoint PUT
- **Ruta**: `PUT /api/v1/cards/{cardId}`
- **Función**: Actualizar una tarjeta existente con nuevos datos
- **Status esperado**: `200 OK`
- **Manejo de errores**:
  - `404 NOT FOUND` - Tarjeta no existe o fue eliminada
  - `400 BAD REQUEST` - Validación fallida (campo inválido, cardNumber duplicado, fecha no futura, etc.)

### 2. Validaciones integradas
✅ Tarjeta debe existir y no estar soft-deleted  
✅ `cardNumber` debe tener exactamente 16 dígitos  
✅ `expirationDate` debe estar en formato YYYY-MM-DD y ser una fecha futura  
✅ `cardNumber` debe ser único por usuario (a menos que sea el mismo número)  
✅ `cardholderName` no puede estar vacío

### 3. Lógica de negocio
- Usa transacciones (`@Transactional`) para consistencia
- Aplica soft delete (compatible con GET que solo devuelve activos)
- Valida unicidad de cardNumber por usuario
- Retorna el `CardResponse` actualizado (con maskedCardNumber)

### 4. Tests unitarios
Se agregaron 5 nuevos tests que validan:
- ✅ Excepción cuando tarjeta no existe
- ✅ Excepción cuando tarjeta está deletada
- ✅ Excepción cuando el nuevo cardNumber ya existe para ese usuario
- ✅ Actualización exitosa de todos los campos
- ✅ Permite mantener el mismo cardNumber si se actualizan otros campos

---

## Cómo usar desde el frontend

### JavaScript/Fetch
```javascript
const cardId = "550e8400-e29b-41d4-a716-446655440000";
const response = await fetch(`http://localhost:8080/api/v1/cards/${cardId}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    cardholderName: "Jane Doe",
    cardNumber: "9876543210123456",
    expirationDate: "2027-06-15"
  })
});

const result = await response.json();
if (!response.ok) {
  console.error('Error:', result.message);
  // Maneja error 400 (validación) o 404 (no encontrada)
} else {
  console.log('Tarjeta actualizada:', result.data);
  // result.data contiene:
  // {
  //   id: UUID,
  //   cardholderName: "Jane Doe",
  //   maskedCardNumber: "************3456",
  //   expirationDate: "2027-06-15"
  // }
}
```

### Axios
```javascript
import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080' });

try {
  const response = await api.put(`/api/v1/cards/${cardId}`, {
    cardholderName: "Jane Doe",
    cardNumber: "9876543210123456",
    expirationDate: "2027-06-15"
  });
  console.log('Tarjeta actualizada:', response.data.data);
} catch (error) {
  if (error.response?.status === 404) {
    console.error('Tarjeta no encontrada');
  } else if (error.response?.status === 400) {
    console.error('Error de validación:', error.response.data.message);
  }
}
```

---

## Pruebas manuales (cmd.exe / Windows)

### Crear una tarjeta (para luego actualizar)
```cmd
curl -i -X POST -H "Content-Type: application/json" -d "{\"cardholderName\":\"John Doe\",\"cardNumber\":\"1234567812345678\",\"expirationDate\":\"2026-12-01\"}" http://localhost:8080/api/v1/cards/550e8400-e29b-41d4-a716-446655440000
```
(Guarda el `id` de la respuesta)

### Actualizar esa tarjeta (PUT)
```cmd
curl -i -X PUT -H "Content-Type: application/json" -d "{\"cardholderName\":\"Jane Doe\",\"cardNumber\":\"9876543210123456\",\"expirationDate\":\"2027-06-15\"}" http://localhost:8080/api/v1/cards/550e8400-e29b-41d4-a716-446655440000
```

### Respuesta esperada (200 OK)
```json
{
  "success": true,
  "message": "Card updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "cardholderName": "Jane Doe",
    "maskedCardNumber": "************3456",
    "expirationDate": "2027-06-15"
  }
}
```

---

## Casos de error a manejar

### 404 - Tarjeta no existe
```json
{
  "success": false,
  "message": "Card not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "data": null
}
```

### 400 - cardNumber no tiene 16 dígitos
```json
{
  "success": false,
  "message": "Card number must be exactly 16 digits",
  "data": null
}
```

### 400 - expirationDate no es futura
```json
{
  "success": false,
  "message": "Expiration date must be in the future",
  "data": null
}
```

### 400 - cardNumber ya existe para este usuario
```json
{
  "success": false,
  "message": "Card number already exists for this user.",
  "data": null
}
```

---

## Archivos modificados/creados

### Backend (Java/Spring Boot)
1. ✅ `CardService.java`
   - Método nuevo: `updateCard(UUID cardId, CreateCardRequest req)`
   - Lógica: valida existencia, soft delete, unicidad y actualiza campos

2. ✅ `CardController.java`
   - Endpoint nuevo: `@PutMapping("/{cardId}")`
   - Mapeo: `PUT /api/v1/cards/{cardId}` → `CardService.updateCard()`

3. ✅ `CardServiceTest.java`
   - 5 tests nuevos que validan el método `updateCard`
   - Todos pasan ✅

### Documentación
- ✅ `CARDS_API_SPECIFICATION.md` - Especificación completa de todos los endpoints

---

## Estado de todos los endpoints de Cards

| Endpoint | Método | Status |
|----------|--------|--------|
| `/api/v1/cards/{userId}` | GET | ✅ Implementado |
| `/api/v1/cards/{userId}` | POST | ✅ Implementado |
| `/api/v1/cards/{cardId}` | PUT | ✅ **NUEVO - Implementado** |
| `/api/v1/cards/{cardId}` | DELETE | ✅ Implementado |

---

## CORS - Confirmación

✅ El origen `http://localhost:5173` ya está permitido en `WebConfig.java`

**No se requieren cambios adicionales de configuración.**

---

## Próximos pasos para el frontend

1. Ubicar el componente donde se editan tarjetas (probablemente en la vista Profile/Payment Methods)
2. Capturar los datos del formulario (cardholderName, cardNumber, expirationDate)
3. Hacer un PUT request a `http://localhost:8080/api/v1/cards/{cardId}`
4. Manejar respuestas 200 (éxito), 400 (validación), 404 (no encontrada)
5. Actualizar la UI con los datos del response

---

## Soporte

Si hay dudas sobre:
- **Formato JSON**: Ver sección "Cómo usar desde el frontend"
- **Validaciones**: Ver sección "Casos de error a manejar"
- **Tests**: Ejecutar `mvn test` para validar localmente
- **Integración**: Consultar `CARDS_API_SPECIFICATION.md` para todos los endpoints

---

## Checklist de aceptación

- ✅ Endpoint PUT implementado y funcional
- ✅ Validaciones de negocio aplicadas (16 dígitos, fecha futura, unicidad)
- ✅ Manejo de errores (400, 404) con mensajes claros
- ✅ CORS configurado para localhost:5173
- ✅ Tests unitarios pasando
- ✅ Documentación completa (CARDS_API_SPECIFICATION.md)
- ✅ Ejemplos de código (Fetch, Axios, curl)

**TODO LISTO PARA PRODUCCIÓN** 🚀

