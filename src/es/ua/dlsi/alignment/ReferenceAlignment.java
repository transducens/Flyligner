/*
 * Copyright (C) 2012 Universitat d'Alacant
 *
 * author: Miquel Esplà Gomis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package es.ua.dlsi.alignment;

/**
 *
 * @author miquel
 */
public class ReferenceAlignment {
    private boolean sure;
    
    private int counterpart;
    
    public ReferenceAlignment(int counterpart, boolean possible){
        this.counterpart=counterpart;
        this.sure=(!possible);
    }

    public boolean isSure() {
        return sure;
    }

    public void setPossible(boolean sure) {
        this.sure = sure;
    }

    public int getCounterpart() {
        return counterpart;
    }

    public void setCounterpart(int c) {
        this.counterpart = c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReferenceAlignment other = (ReferenceAlignment) obj;
        if (this.counterpart != other.counterpart) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.counterpart;
        return hash;
    }
}
