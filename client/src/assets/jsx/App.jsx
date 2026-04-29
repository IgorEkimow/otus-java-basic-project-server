import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../css/App.css';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

function App() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [editingItem, setEditingItem] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: '',
        category: '',
        quantity: 0
    });

    const fetchItems = async () => {
        try {
            setLoading(true);
            const response = await api.get('/items');
            setItems(response.data);
            setError(null);
        } catch (err) {
            setError('Failed to load items. Make sure the server is running on port 8080');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchItems();
    }, []);

    const createItem = async (itemData) => {
        try {
            const response = await api.post('/items', itemData);
            setItems([...items, response.data]);

            return true;
        } catch (err) {
            setError('Failed to create item');

            return false;
        }
    };

    const updateItem = async (itemData) => {
        try {
            const response = await api.put('/items', itemData);
            setItems(items.map(item => item.id === response.data.id ? response.data : item));

            return true;
        } catch (err) {
            setError('Failed to update item');

            return false;
        }
    };

    const deleteItem = async (id) => {
        if (!window.confirm('Are you sure you want to delete this item?')) return;

        try {
            await api.delete(`/items/${id}`);
            setItems(items.filter(item => item.id !== id));
        } catch (err) {
            setError('Failed to delete item');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.name || !formData.price) {
            setError('Name and Price are required');

            return;
        }

        let success;
        if (editingItem) {
            success = await updateItem({ ...formData, id: editingItem.id });
        } else {
            success = await createItem(formData);
        }

        if (success) {
            resetForm();
            setShowModal(false);
        }
    };

    const handleEdit = (item) => {
        setEditingItem(item);
        setFormData({
            name: item.name || '',
            description: item.description || '',
            price: item.price || '',
            category: item.category || '',
            quantity: item.quantity || 0
        });
        setShowModal(true);
    };

    const resetForm = () => {
        setFormData({
            name: '',
            description: '',
            price: '',
            category: '',
            quantity: 0
        });
        setEditingItem(null);
        setError(null);
    };

    const handleAddNew = () => {
        resetForm();
        setShowModal(true);
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
                <p>Loading items...</p>
            </div>
        );
    }

    return (
        <div className="App">
            <header className="header">
                <div className="header-content">
                    <h1>🏗️ Client REST API</h1>
                    <p>REST API Demo with React + Java Backend</p>
                    <div className="api-info">🔗 API Endpoint: <code>{API_BASE_URL}</code></div>
                </div>
            </header>

            <div className="container">
                <div className="toolbar">
                    <button className="btn btn-primary" onClick={handleAddNew}>Add New Item</button>
                    <button className="btn btn-secondary" onClick={fetchItems}>Refresh</button>
                </div>

                {error && (<div className="error-message">❌ {error}</div>)}

                {items.length === 0 ? (
                    <div className="empty-state">
                        <p>No items found. Click "Add New Item" to create one.</p>
                    </div>
                ) : (
                    <div className="items-grid">
                        {items.map((item) => (
                            <div key={item.id} className="item-card">
                                <div className="item-header">
                                    <h3>{item.name}</h3>
                                    <span className="category">{item.category || 'Uncategorized'}</span>
                                </div>
                                <div className="item-price">{item.price?.toFixed(2)} ₽</div>
                                <div className="item-quantity">
                                    📦 Quantity: <strong>{item.quantity || 0}</strong>
                                </div>
                                {item.description && (<p className="item-description">{item.description}</p>)}
                                <div className="item-actions">
                                    <button className="btn-edit" onClick={() => handleEdit(item)}>✏️ Edit</button>
                                    <button className="btn-delete" onClick={() => deleteItem(item.id)}>🗑️ Delete</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal" onClick={() => {setShowModal(false);resetForm();}}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{editingItem ? 'Edit Item' : 'Create New Item'}</h2>
                            <button className="modal-close" onClick={() => {setShowModal(false);resetForm();}}>×</button>
                        </div>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Name *</label>
                                <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} placeholder="Enter item name" required/>
                            </div>
                            <div className="form-group">
                                <label>Description</label>
                                <textarea value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} placeholder="Enter description (optional)" rows="3"/>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Price *</label>
                                    <input type="number" step="0.01" value={formData.price} onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })} placeholder="0.00" required/>
                                </div>
                                <div className="form-group">
                                    <label>Quantity</label>
                                    <input type="number" value={formData.quantity} onChange={(e) => setFormData({ ...formData, quantity: parseInt(e.target.value) || 0 })} placeholder="0"/>
                                </div>
                            </div>
                            <div className="form-group">
                                <label>Category</label>
                                <input type="text" value={formData.category} onChange={(e) => setFormData({ ...formData, category: e.target.value })} placeholder="Enter category (optional)"/>
                            </div>
                            <div className="form-actions">
                                <button type="submit" className="btn btn-primary">{editingItem ? 'Update Item' : 'Create Item'}</button>
                                <button type="button" className="btn btn-secondary" onClick={() => {setShowModal(false);resetForm();}}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default App;