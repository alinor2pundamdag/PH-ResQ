# PH-ResQ Vanguard 2026: Phase 2 - Crisis-Grade UI/UX & Canvas Physics Animation

## Objective
Implement a high-performance, cognitive-load-free mobile UI featuring responsive safe-area layout management, high-contrast accessibility mode, and custom canvas physics simulations (Infinite Philippine Flag Color-Looping Header and Wind-Swaying Animation) designed for low-end Android devices with zero CPU/GPU frame drops.

---

## Step 1: Responsive Layout & Safe-Area Header Component
Create `src/components/header/CrisisHeader.tsx` to handle dynamic window insets, notches, and high-contrast toggles:

```typescript
import React, { useState } from 'react';

export const CrisisHeader: React.FC = () => {
  const [highContrast, setHighContrast] = useState<boolean>(false);

  const toggleHighContrast = () => {
    setHighContrast(!highContrast);
    document.body.classList.toggle('high-contrast-mode', !highContrast);
  };

  return (
    <header className={`crisis-header ${highContrast ? 'contrast-active' : ''}`}>
      <div className="flag-canvas-container">
        <canvas id="phil-flag-canvas" width="300" height="60" />
      </div>
      <div className="header-controls">
        <h1 className="app-title">PH-ResQ Vanguard</h1>
        <button 
          onClick={toggleHighContrast}
          className="accessibility-btn"
          aria-label="Toggle High Contrast Mode"
        >
          {highContrast ? 'Normal Mode' : 'High Contrast (PWD/Senior)'}
        </button>
      </div>
    </header>
  );
};
```

---

## Step 2: Wind-Swaying Canvas Physics Simulation
Create `src/components/header/FlagPhysics.ts` to render the infinite color-looping header with wave physics:

```typescript
export class FlagPhysicsRenderer {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private animationFrameId: number = 0;
  private time: number = 0;

  constructor(canvasId: string) {
    this.canvas = document.getElementById(canvasId) as HTMLCanvasElement;
    this.ctx = this.canvas.getContext('2d')!;
  }

  public startAnimation(): void {
    const render = () => {
      this.time += 0.05;
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

      // Render Philippine flag color bands with sine wave distortion
      const gradient = this.ctx.createLinearGradient(0, 0, this.canvas.width, 0);
      gradient.addColorStop(0, '#0038A8'); // Royal Blue
      gradient.addColorStop(0.5, '#CE1126'); // Crimson Red
      gradient.addColorStop(1, '#FCD116'); // Yellow Sun Gold

      this.ctx.fillStyle = gradient;
      this.ctx.beginPath();
      
      for (let x = 0; x < this.canvas.width; x++) {
        const y = Math.sin(x * 0.03 + this.time) * 5 + 30;
        if (x === 0) this.ctx.moveTo(x, y);
        else this.ctx.lineTo(x, y);
      }

      this.ctx.lineTo(this.canvas.width, this.canvas.height);
      this.ctx.lineTo(0, this.canvas.height);
      this.ctx.closePath();
      this.ctx.fill();

      this.animationFrameId = requestAnimationFrame(render);
    };
    render();
  }

  public stopAnimation(): void {
    cancelAnimationFrame(this.animationFrameId);
  }
}
```

---

## Step 3: Crisis-Grade Accessibility CSS Styling
Add high-contrast rules to ensure maximum visibility during daylight or power blackout scenarios:

```css
body.high-contrast-mode {
  background-color: #000000 !important;
  color: #FFFF00 !important;
}

body.high-contrast-mode .crisis-header {
  background-color: #000000;
  border-bottom: 3px solid #FFFF00;
}

.crisis-header {
  padding-top: env(safe-area-inset-top);
  display: flex;
  flex-direction: column;
  width: 100%;
}
```
